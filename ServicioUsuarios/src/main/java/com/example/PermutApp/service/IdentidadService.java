package com.example.PermutApp.service;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.Entities.VerificacionIdentidad;
import com.example.PermutApp.model.Entities.VerificacionIdentidad.EstadoVerificacion;
import com.example.PermutApp.model.dto.VerificacionIdentidadDto;
import com.example.PermutApp.repository.UsuarioRepository;
import com.example.PermutApp.repository.VerificacionIdentidadRepository;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CompareFacesRequest;
import software.amazon.awssdk.services.rekognition.model.CompareFacesResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;

@Service
public class IdentidadService {

   private static final Pattern RUN_PATTERN = Pattern.compile("(\\d{1,2}\\.?\\d{3}\\.?\\d{3})\\s*-?\\s*([0-9Kk])");
   private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;

   private final UsuarioRepository usuarioRepository;
   private final VerificacionIdentidadRepository verificacionRepository;
   private final RekognitionClient rekognitionClient;
   private final TextractClient textractClient;
   private final double faceThreshold;

   public IdentidadService(
         UsuarioRepository usuarioRepository,
         VerificacionIdentidadRepository verificacionRepository,
         RekognitionClient rekognitionClient,
         TextractClient textractClient,
         @Value("${identity.face-threshold:85}") double faceThreshold) {
      this.usuarioRepository = usuarioRepository;
      this.verificacionRepository = verificacionRepository;
      this.rekognitionClient = rekognitionClient;
      this.textractClient = textractClient;
      this.faceThreshold = faceThreshold;
   }


   public void validarUsuarioAutenticado(int usuarioId, String emailAutenticado) {
      Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
      if (!usuario.getUsu_email().equalsIgnoreCase(emailAutenticado)) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes verificar la identidad de otro usuario");
      }
   }

   public VerificacionIdentidadDto verificar(int usuarioId, MultipartFile carnet, MultipartFile selfie) {
      Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

      byte[] carnetBytes = leerImagen(carnet, "carnet");
      byte[] selfieBytes = leerImagen(selfie, "selfie");

      double similarity = compararRostros(carnetBytes, selfieBytes);
      OcrResultado ocr = extraerDatosCarnet(carnetBytes);
      boolean faceMatch = similarity >= faceThreshold;
      boolean runMatch = ocr.runDetectado()
            .map(run -> normalizarRun(run).equals(normalizarRun(usuario.getUsu_numrun() + "-" + usuario.getUsu_dvrun())))
            .orElse(false);

      EstadoVerificacion estado;
      String observacion;
      if (!faceMatch) {
         estado = EstadoVerificacion.RECHAZADA;
         observacion = "La similitud facial no supera el umbral configurado.";
      } else if (ocr.runDetectado().isEmpty()) {
         estado = EstadoVerificacion.REVISION_MANUAL;
         observacion = "El rostro coincide, pero el OCR no pudo extraer el RUN del documento.";
      } else if (runMatch) {
         estado = EstadoVerificacion.APROBADA;
         observacion = "Rostro y RUN coinciden.";
      } else {
         estado = EstadoVerificacion.RECHAZADA;
         observacion = "El rostro coincide, pero el RUN detectado no coincide con el usuario registrado.";
      }

      VerificacionIdentidad verificacion = new VerificacionIdentidad();
      verificacion.setUsu_id(usuario.getUsu_id());
      verificacion.setVer_estado(estado);
      verificacion.setVer_face_similarity(similarity);
      verificacion.setVer_face_threshold(faceThreshold);
      verificacion.setVer_face_provider("AMAZON_REKOGNITION");
      verificacion.setVer_ocr_run_detectado(ocr.runDetectado().orElse(null));
      verificacion.setVer_ocr_nombre_detectado(ocr.nombreDetectado().orElse(null));
      verificacion.setVer_run_match(runMatch);
      verificacion.setVer_ocr_provider("AMAZON_TEXTRACT");
      verificacion.setVer_fecha(Instant.now());
      verificacion.setVer_observacion(observacion);

      return convertirADto(verificacionRepository.save(verificacion));
   }

   public VerificacionIdentidadDto obtenerUltimaVerificacion(int usuarioId) {
      if (!usuarioRepository.existsById(usuarioId)) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }
      return verificacionRepository.findUltimaByUsuario(usuarioId)
            .map(this::convertirADto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no tiene verificaciones"));
   }

   public boolean usuarioEstaVerificado(int usuarioId) {
      return verificacionRepository.findUltimaByUsuario(usuarioId)
            .map(verificacion -> verificacion.getVer_estado() == EstadoVerificacion.APROBADA)
            .orElse(false);
   }

   public VerificacionIdentidadDto convertirADto(VerificacionIdentidad verificacion) {
      return new VerificacionIdentidadDto(
            verificacion.getVer_id(),
            verificacion.getUsu_id(),
            verificacion.getVer_estado(),
            verificacion.getVer_face_similarity(),
            verificacion.getVer_face_threshold(),
            verificacion.getVer_face_provider(),
            verificacion.getVer_ocr_run_detectado(),
            verificacion.getVer_ocr_nombre_detectado(),
            verificacion.getVer_run_match(),
            verificacion.getVer_ocr_provider(),
            verificacion.getVer_fecha(),
            verificacion.getVer_observacion());
   }

   private byte[] leerImagen(MultipartFile archivo, String nombreCampo) {
      if (archivo == null || archivo.isEmpty()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen de " + nombreCampo + " es obligatoria");
      }
      if (archivo.getSize() > MAX_IMAGE_BYTES) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen de " + nombreCampo + " supera 5 MB");
      }
      String contentType = archivo.getContentType();
      if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen de " + nombreCampo + " debe ser JPG o PNG");
      }
      try {
         return archivo.getBytes();
      } catch (IOException e) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No fue posible leer la imagen de " + nombreCampo);
      }
   }

   private double compararRostros(byte[] carnetBytes, byte[] selfieBytes) {
      CompareFacesResponse response = rekognitionClient.compareFaces(CompareFacesRequest.builder()
            .sourceImage(Image.builder().bytes(SdkBytes.fromByteArray(carnetBytes)).build())
            .targetImage(Image.builder().bytes(SdkBytes.fromByteArray(selfieBytes)).build())
            .similarityThreshold((float) faceThreshold)
            .build());

      return response.faceMatches().stream()
            .map(match -> match.similarity().doubleValue())
            .max(Double::compareTo)
            .orElse(0.0);
   }

   private OcrResultado extraerDatosCarnet(byte[] carnetBytes) {
      String texto = textractClient.detectDocumentText(DetectDocumentTextRequest.builder()
            .document(Document.builder().bytes(SdkBytes.fromByteArray(carnetBytes)).build())
            .build())
            .blocks()
            .stream()
            .filter(block -> block.blockType() == BlockType.LINE)
            .map(block -> block.text() == null ? "" : block.text())
            .reduce("", (actual, linea) -> actual + "\n" + linea);

      Matcher matcher = RUN_PATTERN.matcher(texto);
      Optional<String> run = matcher.find()
            ? Optional.of(matcher.group(1).replace(".", "") + "-" + matcher.group(2).toUpperCase(Locale.ROOT))
            : Optional.empty();

      return new OcrResultado(run, detectarNombre(texto));
   }

   private Optional<String> detectarNombre(String texto) {
      String[] lineas = texto.split("\\R");
      for (String linea : lineas) {
         String limpia = linea.trim();
         String normalizada = Normalizer.normalize(limpia, Normalizer.Form.NFD)
               .replaceAll("\\p{M}", "")
               .toUpperCase(Locale.ROOT);
         if (normalizada.contains("NOMBRE") || normalizada.contains("APELLIDO")) {
            return Optional.of(limpia);
         }
      }
      return Optional.empty();
   }

   private String normalizarRun(String run) {
      return run.replace(".", "")
            .replace(" ", "")
            .toUpperCase(Locale.ROOT);
   }

   private record OcrResultado(Optional<String> runDetectado, Optional<String> nombreDetectado) {
   }
}

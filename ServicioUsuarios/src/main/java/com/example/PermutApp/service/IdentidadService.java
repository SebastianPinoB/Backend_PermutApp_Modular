package com.example.PermutApp.service;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
   private final List<String> reviewerEmails;

   public IdentidadService(
         UsuarioRepository usuarioRepository,
         VerificacionIdentidadRepository verificacionRepository,
         RekognitionClient rekognitionClient,
         TextractClient textractClient,
         @Value("${identity.face-threshold:85}") double faceThreshold,
         @Value("${identity.reviewer-emails:}") String reviewerEmails) {
      this.usuarioRepository = usuarioRepository;
      this.verificacionRepository = verificacionRepository;
      this.rekognitionClient = rekognitionClient;
      this.textractClient = textractClient;
      this.faceThreshold = faceThreshold;
      this.reviewerEmails = parseReviewerEmails(reviewerEmails);
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
      OcrResultado ocr = extraerDatosCarnet(carnetBytes, usuario);
      boolean faceMatch = similarity >= faceThreshold;
      boolean runMatch = ocr.runDetectado()
            .map(run -> normalizarRun(run).equals(normalizarRun(usuario.getUsu_numrun() + "-" + usuario.getUsu_dvrun())))
            .orElse(false);
      boolean nombreMatch = ocr.nombreMatch();

      EstadoVerificacion estado;
      String observacion;
      if (!faceMatch) {
         estado = EstadoVerificacion.RECHAZADA;
         observacion = "La similitud facial no supera el umbral configurado.";
      } else if (ocr.runDetectado().isEmpty()) {
         estado = EstadoVerificacion.REVISION_MANUAL;
         observacion = "El rostro coincide, pero el OCR no pudo extraer el RUN del documento.";
      } else if (!runMatch) {
         estado = EstadoVerificacion.RECHAZADA;
         observacion = "El rostro coincide, pero el RUN detectado no coincide con el usuario registrado.";
      } else if (!nombreMatch) {
         estado = EstadoVerificacion.REVISION_MANUAL;
         observacion = "Rostro y RUN coinciden, pero el OCR no confirmo nombre y apellido.";
      } else {
         estado = EstadoVerificacion.APROBADA;
         observacion = "Rostro, RUN y nombre coinciden.";
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
      verificacion.setVer_nombre_match(nombreMatch);
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

   public List<VerificacionIdentidadDto> listarRevisionManual(String emailRevisor) {
      validarRevisor(emailRevisor);
      return verificacionRepository.findRevisionManual(EstadoVerificacion.REVISION_MANUAL.name())
            .stream()
            .map(this::convertirADto)
            .toList();
   }

   public VerificacionIdentidadDto resolverRevisionManual(
         int verificacionId,
         EstadoVerificacion estado,
         String observacion,
         String emailRevisor) {
      validarRevisor(emailRevisor);
      if (estado != EstadoVerificacion.APROBADA && estado != EstadoVerificacion.RECHAZADA) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La revision manual solo puede aprobar o rechazar");
      }

      VerificacionIdentidad verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verificacion no encontrada"));
      if (verificacion.getVer_estado() != EstadoVerificacion.REVISION_MANUAL) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La verificacion no esta pendiente de revision manual");
      }

      verificacion.setVer_estado(estado);
      verificacion.setVer_revisor_email(emailRevisor.toLowerCase(Locale.ROOT));
      verificacion.setVer_fecha_revision(Instant.now());
      verificacion.setVer_observacion(limpiarObservacionRevision(observacion, estado));
      return convertirADto(verificacionRepository.save(verificacion));
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
            verificacion.getVer_nombre_match(),
            verificacion.getVer_ocr_provider(),
            verificacion.getVer_fecha(),
            verificacion.getVer_observacion(),
            verificacion.getVer_revisor_email(),
            verificacion.getVer_fecha_revision());
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

   private OcrResultado extraerDatosCarnet(byte[] carnetBytes, Usuario usuario) {
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

      String textoNormalizado = normalizarTexto(texto);
      List<String> coincidenciasNombre = obtenerTokensUsuario(usuario).stream()
            .filter(token -> contieneToken(textoNormalizado, token))
            .toList();
      boolean nombreMatch = nombrePrincipalCoincide(textoNormalizado, usuario);
      Optional<String> nombreDetectado = coincidenciasNombre.isEmpty()
            ? Optional.empty()
            : Optional.of(String.join(" ", coincidenciasNombre));

      return new OcrResultado(run, nombreDetectado, nombreMatch);
   }

   private boolean nombrePrincipalCoincide(String textoNormalizado, Usuario usuario) {
      String primerNombre = primerToken(usuario.getUsu_pri_nombre());
      String primerApellido = primerToken(usuario.getUsu_pri_apellido());
      if (primerNombre.isBlank() || primerApellido.isBlank()) {
         return false;
      }
      return contieneToken(textoNormalizado, primerNombre) && contieneToken(textoNormalizado, primerApellido);
   }

   private List<String> obtenerTokensUsuario(Usuario usuario) {
      List<String> tokens = new ArrayList<>();
      agregarToken(tokens, usuario.getUsu_pri_nombre());
      agregarToken(tokens, usuario.getUsu_seg_nombre());
      agregarToken(tokens, usuario.getUsu_pri_apellido());
      agregarToken(tokens, usuario.getUsu_seg_apellido());
      return tokens;
   }

   private void agregarToken(List<String> tokens, String valor) {
      String token = primerToken(valor);
      if (!token.isBlank() && !tokens.contains(token)) {
         tokens.add(token);
      }
   }

   private String primerToken(String valor) {
      if (valor == null) {
         return "";
      }
      String normalizado = normalizarTexto(valor).trim();
      int espacio = normalizado.indexOf(' ');
      return espacio >= 0 ? normalizado.substring(0, espacio) : normalizado;
   }

   private boolean contieneToken(String textoNormalizado, String token) {
      return Pattern.compile("(^|\\s)" + Pattern.quote(token) + "($|\\s)").matcher(textoNormalizado).find();
   }

   private String normalizarRun(String run) {
      return run.replace(".", "")
            .replace(" ", "")
            .toUpperCase(Locale.ROOT);
   }

   private String normalizarTexto(String texto) {
      return Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^A-Za-z0-9Kk]+", " ")
            .toUpperCase(Locale.ROOT)
            .replaceAll("\\s+", " ")
            .trim();
   }

   private List<String> parseReviewerEmails(String rawEmails) {
      if (rawEmails == null || rawEmails.isBlank()) {
         return List.of();
      }
      List<String> emails = new ArrayList<>();
      for (String email : rawEmails.split(",")) {
         String limpio = email.trim().toLowerCase(Locale.ROOT);
         if (!limpio.isBlank()) {
            emails.add(limpio);
         }
      }
      return emails;
   }

   private void validarRevisor(String emailRevisor) {
      String email = emailRevisor == null ? "" : emailRevisor.toLowerCase(Locale.ROOT);
      if (reviewerEmails.isEmpty() || !reviewerEmails.contains(email)) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para revisar verificaciones manuales");
      }
   }

   private String limpiarObservacionRevision(String observacion, EstadoVerificacion estado) {
      if (observacion == null || observacion.isBlank()) {
         return estado == EstadoVerificacion.APROBADA
               ? "Revision manual aprobada."
               : "Revision manual rechazada.";
      }
      return observacion.trim();
   }

   private record OcrResultado(Optional<String> runDetectado, Optional<String> nombreDetectado, boolean nombreMatch) {
   }
}

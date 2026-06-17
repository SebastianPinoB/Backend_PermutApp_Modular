package com.example.PermutApp.service;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.dto.NotificacionDto;
import com.example.PermutApp.model.dto.SuscripcionDto;
import com.example.PermutApp.model.entities.CanalNotificacion;
import com.example.PermutApp.model.entities.Notificacion;
import com.example.PermutApp.model.entities.SuscripcionNotificacion;
import com.example.PermutApp.model.request.CrearEventoNotificacionRequest;
import com.example.PermutApp.model.request.RegistrarSuscripcionRequest;
import com.example.PermutApp.repository.NotificacionRepository;
import com.example.PermutApp.repository.SuscripcionNotificacionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotificacionService {
   private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
   private final NotificacionRepository notificacionRepository;
   private final SuscripcionNotificacionRepository suscripcionRepository;
   private final ExpoPushSender expoPushSender;
   private final WebPushSender webPushSender;
   private final ObjectMapper objectMapper;

   public NotificacionService(
         NotificacionRepository notificacionRepository,
         SuscripcionNotificacionRepository suscripcionRepository,
         ExpoPushSender expoPushSender,
         WebPushSender webPushSender) {
      this.notificacionRepository = notificacionRepository;
      this.suscripcionRepository = suscripcionRepository;
      this.expoPushSender = expoPushSender;
      this.webPushSender = webPushSender;
      this.objectMapper = new ObjectMapper();
   }

   public SuscripcionDto registrarSuscripcion(int usuarioId, RegistrarSuscripcionRequest request) {
      validarSuscripcion(request);
      Instant ahora = Instant.now();
      SuscripcionNotificacion suscripcion = suscripcionRepository.buscarPorDestino(request.destino().trim())
            .orElseGet(() -> {
               SuscripcionNotificacion nueva = new SuscripcionNotificacion();
               nueva.setNsus_fecha_creacion(ahora);
               return nueva;
            });
      suscripcion.setUsu_id(usuarioId);
      suscripcion.setNsus_canal(request.canal());
      suscripcion.setNsus_destino(request.destino().trim());
      suscripcion.setNsus_p256dh(limpiar(request.p256dh()));
      suscripcion.setNsus_auth(limpiar(request.auth()));
      suscripcion.setNsus_plataforma(request.plataforma().trim().toLowerCase());
      suscripcion.setNsus_activa(true);
      suscripcion.setNsus_fecha_actualizacion(ahora);
      SuscripcionNotificacion guardada = suscripcionRepository.save(suscripcion);
      log.info("Suscripcion {} registrada para usuario {} por {}", guardada.getNsus_id(), usuarioId, request.canal());
      return convertirSuscripcion(guardada);
   }

   public void desactivarSuscripcion(int usuarioId, int suscripcionId) {
      if (suscripcionRepository.desactivar(suscripcionId, usuarioId) == 0) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Suscripcion no encontrada");
      }
   }

   public List<NotificacionDto> listar(int usuarioId) {
      return notificacionRepository.listarPorUsuario(usuarioId).stream().map(this::convertirNotificacion).toList();
   }

   public long contarNoLeidas(int usuarioId) {
      return notificacionRepository.contarNoLeidas(usuarioId);
   }

   public void marcarLeida(int usuarioId, int notificacionId) {
      if (notificacionRepository.marcarLeida(notificacionId, usuarioId) == 0) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificacion no encontrada");
      }
   }

   public void marcarTodasLeidas(int usuarioId) {
      notificacionRepository.marcarTodasLeidas(usuarioId);
   }

   public void eliminar(int usuarioId, int notificacionId) {
      if (notificacionRepository.eliminar(notificacionId, usuarioId) == 0) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificacion no encontrada");
      }
   }

   public NotificacionDto crearEvento(CrearEventoNotificacionRequest request) {
      Contenido contenido = construirContenido(request);
      Notificacion notificacion = new Notificacion();
      notificacion.setUsu_id(request.usuarioId());
      notificacion.setNotif_tipo(request.tipo());
      notificacion.setNotif_titulo(contenido.titulo());
      notificacion.setNotif_cuerpo(contenido.cuerpo());
      notificacion.setNotif_datos(escribirJson(contenido.datos()));
      notificacion.setNotif_fecha(Instant.now());
      notificacion.setNotif_leida(false);
      Notificacion guardada = notificacionRepository.save(notificacion);
      log.info(
            "Evento {} persistido como notificacion {} para usuario {}",
            request.tipo(),
            guardada.getNotif_id(),
            request.usuarioId());
      enviarPush(request.usuarioId(), guardada, contenido.datos());
      return convertirNotificacion(guardada);
   }

   private void enviarPush(int usuarioId, Notificacion notificacion, Map<String, Object> datos) {
      String payload = escribirJson(Map.of(
            "title", notificacion.getNotif_titulo(),
            "body", notificacion.getNotif_cuerpo(),
            "data", datos));
      for (SuscripcionNotificacion suscripcion : suscripcionRepository.listarActivasPorUsuario(usuarioId)) {
         ResultadoEnvioPush resultado = suscripcion.getNsus_canal() == CanalNotificacion.EXPO
               ? expoPushSender.enviar(
                     suscripcion.getNsus_destino(),
                     notificacion.getNotif_titulo(),
                     notificacion.getNotif_cuerpo(),
                     datos)
               : webPushSender.enviar(
                     suscripcion.getNsus_destino(),
                     suscripcion.getNsus_p256dh(),
                     suscripcion.getNsus_auth(),
                     payload);
         if (resultado == ResultadoEnvioPush.SUSCRIPCION_INVALIDA) {
            suscripcionRepository.desactivarPorId(suscripcion.getNsus_id());
            log.warn("Suscripcion {} desactivada porque el proveedor la rechazo", suscripcion.getNsus_id());
         } else if (resultado == ResultadoEnvioPush.ERROR_TEMPORAL) {
            log.warn(
                  "No se pudo entregar la notificacion {} mediante la suscripcion {}",
                  notificacion.getNotif_id(),
                  suscripcion.getNsus_id());
         } else {
            log.info(
                  "Notificacion {} entregada mediante la suscripcion {}",
                  notificacion.getNotif_id(),
                  suscripcion.getNsus_id());
         }
      }
   }

   private Contenido construirContenido(CrearEventoNotificacionRequest request) {
      String tituloPublicacion = limpiar(request.publicacionTitulo());
      Map<String, Object> datos = new LinkedHashMap<>();
      datos.put("tipo", request.tipo().name());
      if (request.conversacionId() != null) datos.put("conversacionId", request.conversacionId());
      if (request.publicacionId() != null) datos.put("publicacionId", request.publicacionId());

      return switch (request.tipo()) {
         case MENSAJE_NUEVO -> contenidoChat("Nuevo mensaje", "Tienes un nuevo mensaje en PermutApp.", request, datos);
         case PROPUESTA_PERMUTA -> {
            String cuerpo = tituloPublicacion == null
                  ? "Recibiste una nueva propuesta de permuta."
                  : "Alguien quiere permutar por " + tituloPublicacion + ".";
            yield contenidoChat("Nueva propuesta de permuta", cuerpo, request, datos);
         }
         case OFERTA_COMPARTIDA ->
            contenidoChat("Nueva oferta", "Compartieron una permuta en el chat.", request, datos);
         case FINALIZACION_SOLICITADA ->
            contenidoChat("Confirmacion pendiente", "La otra persona propuso finalizar la permuta.", request, datos);
         case PERMUTA_FINALIZADA ->
            contenidoChat("Permuta finalizada", "Ambas personas confirmaron la permuta. Ya puedes valorar.", request, datos);
         case VALORACION_RECIBIDA ->
            contenidoChat("Nueva valoracion", "Recibiste una valoracion por una permuta finalizada.", request, datos);
         case IDENTIDAD_APROBADA -> {
            datos.put("ruta", "/profile");
            yield new Contenido("Identidad verificada", "Tu identidad fue aprobada correctamente.", datos);
         }
         case IDENTIDAD_RECHAZADA -> {
            datos.put("ruta", "/verify-identity");
            yield new Contenido("Verificacion rechazada", "Revisa el resultado e intenta verificar tu identidad nuevamente.", datos);
         }
         case IDENTIDAD_REVISION -> {
            datos.put("ruta", "/profile");
            yield new Contenido("Verificacion en revision", "Tu identidad sera revisada manualmente.", datos);
         }
      };
   }

   private Contenido contenidoChat(
         String titulo,
         String cuerpo,
         CrearEventoNotificacionRequest request,
         Map<String, Object> datos) {
      datos.put("ruta", "/chat/" + request.conversacionId());
      return new Contenido(titulo, cuerpo, datos);
   }

   private void validarSuscripcion(RegistrarSuscripcionRequest request) {
      String destino = request.destino().trim();
      if (request.canal() == CanalNotificacion.EXPO) {
         boolean valido = destino.startsWith("ExponentPushToken[") || destino.startsWith("ExpoPushToken[");
         if (!valido || !destino.endsWith("]")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token Expo invalido");
         }
         return;
      }
      if (request.p256dh() == null || request.p256dh().isBlank()
            || request.auth() == null || request.auth().isBlank()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La suscripcion web requiere p256dh y auth");
      }
      try {
         URI uri = URI.create(destino);
         if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException();
         }
      } catch (Exception e) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endpoint Web Push invalido");
      }
   }

   private NotificacionDto convertirNotificacion(Notificacion entity) {
      try {
         Map<String, Object> datos = objectMapper.readValue(entity.getNotif_datos(), new TypeReference<>() { });
         return new NotificacionDto(entity.getNotif_id(), entity.getNotif_tipo(), entity.getNotif_titulo(),
               entity.getNotif_cuerpo(), datos, entity.getNotif_fecha(), entity.isNotif_leida());
      } catch (Exception e) {
         throw new IllegalStateException("Datos de notificacion invalidos", e);
      }
   }

   private SuscripcionDto convertirSuscripcion(SuscripcionNotificacion entity) {
      return new SuscripcionDto(
            entity.getNsus_id(),
            entity.getNsus_canal(),
            entity.getNsus_plataforma(),
            entity.isNsus_activa());
   }

   private String escribirJson(Object value) {
      try {
         return objectMapper.writeValueAsString(value);
      } catch (Exception e) {
         throw new IllegalStateException("No fue posible serializar la notificacion", e);
      }
   }

   private String limpiar(String value) {
      if (value == null || value.trim().isEmpty()) return null;
      return value.trim();
   }

   private record Contenido(String titulo, String cuerpo, Map<String, Object> datos) { }
}

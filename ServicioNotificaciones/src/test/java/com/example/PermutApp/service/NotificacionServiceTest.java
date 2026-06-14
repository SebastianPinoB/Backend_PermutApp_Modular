package com.example.PermutApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.entities.CanalNotificacion;
import com.example.PermutApp.model.entities.Notificacion;
import com.example.PermutApp.model.entities.SuscripcionNotificacion;
import com.example.PermutApp.model.entities.TipoNotificacion;
import com.example.PermutApp.model.request.CrearEventoNotificacionRequest;
import com.example.PermutApp.model.request.RegistrarSuscripcionRequest;
import com.example.PermutApp.repository.NotificacionRepository;
import com.example.PermutApp.repository.SuscripcionNotificacionRepository;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {
   @Mock private NotificacionRepository notificacionRepository;
   @Mock private SuscripcionNotificacionRepository suscripcionRepository;
   @Mock private ExpoPushSender expoPushSender;
   @Mock private WebPushSender webPushSender;

   private NotificacionService service;

   @BeforeEach
   void setUp() {
      service = new NotificacionService(
            notificacionRepository,
            suscripcionRepository,
            expoPushSender,
            webPushSender);
   }

   @Test
   void rechazaTokenExpoSimuladoOInvalido() {
      ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.registrarSuscripcion(
                  7,
                  new RegistrarSuscripcionRequest(
                        CanalNotificacion.EXPO,
                        "token-simulado",
                        null,
                        null,
                        "android")));
      assertEquals(400, error.getStatusCode().value());
   }

   @Test
   void persisteEventoYDesactivaSuscripcionRechazadaPorExpo() {
      SuscripcionNotificacion subscription = new SuscripcionNotificacion();
      subscription.setNsus_id(8);
      subscription.setUsu_id(7);
      subscription.setNsus_canal(CanalNotificacion.EXPO);
      subscription.setNsus_destino("ExponentPushToken[real-token]");
      subscription.setNsus_activa(true);
      subscription.setNsus_fecha_creacion(Instant.now());
      subscription.setNsus_fecha_actualizacion(Instant.now());
      when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
         Notificacion notificacion = invocation.getArgument(0);
         notificacion.setNotif_id(11);
         return notificacion;
      });
      when(suscripcionRepository.listarActivasPorUsuario(7)).thenReturn(List.of(subscription));
      when(expoPushSender.enviar(anyString(), anyString(), anyString(), anyMap()))
            .thenReturn(ResultadoEnvioPush.SUSCRIPCION_INVALIDA);
      when(suscripcionRepository.desactivarPorId(anyInt())).thenReturn(1);

      var resultado = service.crearEvento(new CrearEventoNotificacionRequest(
            7,
            TipoNotificacion.OFERTA_COMPARTIDA,
            22,
            30,
            null));

      assertEquals(11, resultado.notif_id());
      assertEquals(TipoNotificacion.OFERTA_COMPARTIDA, resultado.notif_tipo());
      verify(suscripcionRepository).desactivarPorId(8);
   }
}

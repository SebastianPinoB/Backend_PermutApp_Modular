package com.example.PermutApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.entities.Conversacion;
import com.example.PermutApp.model.entities.EstadoConversacion;
import com.example.PermutApp.model.entities.Mensaje;
import com.example.PermutApp.model.entities.Valoracion;
import com.example.PermutApp.model.request.CrearValoracion;
import com.example.PermutApp.repository.ConversacionRepository;
import com.example.PermutApp.repository.MensajeRepository;
import com.example.PermutApp.repository.ValoracionRepository;
import com.example.PermutApp.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MensajeriaServiceTest {
   @Mock private ConversacionRepository conversacionRepository;
   @Mock private MensajeRepository mensajeRepository;
   @Mock private ValoracionRepository valoracionRepository;
   @Mock(answer = Answers.RETURNS_DEEP_STUBS) private WebClient publicacionWebClient;
   @Mock(answer = Answers.RETURNS_DEEP_STUBS) private WebClient productoWebClient;
   @Mock private NotificacionClient notificacionClient;
   @Mock private PublicacionClient publicacionClient;
   @Mock private JwtService jwtService;

   private MensajeriaService service;

   @BeforeEach
   void setUp() {
      service = new MensajeriaService(
            conversacionRepository,
            mensajeRepository,
            valoracionRepository,
            publicacionWebClient,
            productoWebClient,
            notificacionClient,
            publicacionClient,
            jwtService,
            new ObjectMapper());
      when(jwtService.esTokenValido("token")).thenReturn(true);
      when(jwtService.obtenerUsuarioId("token")).thenReturn(2);
      when(mensajeRepository.buscarUltimoPorConversacion(9)).thenReturn(Optional.empty());
      when(valoracionRepository.buscarPorConversacionYEvaluador(9, 2)).thenReturn(Optional.empty());
      when(conversacionRepository.save(any(Conversacion.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(invocation -> invocation.getArgument(0));
   }

   @Test
   void noPermiteSolicitarFinalizacionAntesDeDiezMensajes() {
      Conversacion conversacion = conversacion(EstadoConversacion.NEGOCIANDO);
      when(conversacionRepository.findById(9)).thenReturn(Optional.of(conversacion));
      when(mensajeRepository.contarMensajesUsuario(9)).thenReturn(9L);

      ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.solicitarFinalizacion(9, 2, "Bearer token"));

      assertEquals(400, error.getStatusCode().value());
      verify(conversacionRepository, never()).save(any());
   }

   @Test
   void soloLaOtraPersonaPuedeConfirmar() {
      Conversacion conversacion = conversacion(EstadoConversacion.FINALIZACION_PENDIENTE);
      conversacion.setFinalizacion_solicitada_por(2);
      when(conversacionRepository.findById(9)).thenReturn(Optional.of(conversacion));

      ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.confirmarFinalizacion(9, 2, "Bearer token"));

      assertEquals(400, error.getStatusCode().value());
      verify(publicacionClient, never()).finalizarPermuta(any(Integer.class), any(Integer.class));
   }

   @Test
   void confirmarRetiraAmbasPublicacionesYFinaliza() {
      Conversacion conversacion = conversacion(EstadoConversacion.FINALIZACION_PENDIENTE);
      conversacion.setFinalizacion_solicitada_por(1);
      when(conversacionRepository.findById(9)).thenReturn(Optional.of(conversacion));
      when(mensajeRepository.contarMensajesUsuario(9)).thenReturn(10L);

      var resultado = service.confirmarFinalizacion(9, 2, "Bearer token");

      verify(publicacionClient).finalizarPermuta(30, 40);
      assertEquals(EstadoConversacion.FINALIZADA, resultado.conv_estado());
      assertEquals(false, resultado.conv_activa());
   }

   @Test
   void valoracionEsUnicaPorConversacionYEvaluador() {
      Conversacion conversacion = conversacion(EstadoConversacion.FINALIZADA);
      when(conversacionRepository.findById(9)).thenReturn(Optional.of(conversacion));
      when(valoracionRepository.buscarPorConversacionYEvaluador(9, 2))
            .thenReturn(Optional.of(new Valoracion()));

      ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.valorar(9, new CrearValoracion(2, 5, "Excelente intercambio"), "Bearer token"));

      assertEquals(409, error.getStatusCode().value());
      verify(valoracionRepository, never()).save(any());
   }

   private Conversacion conversacion(EstadoConversacion estado) {
      Conversacion conversacion = new Conversacion();
      conversacion.setConv_id(9);
      conversacion.setPubl_id(30);
      conversacion.setProd_id(20);
      conversacion.setPubl_autor_id(1);
      conversacion.setInteresado_id(2);
      conversacion.setPubl_ofrecida_id(40);
      conversacion.setProd_ofrecido_id(21);
      conversacion.setConv_fech_creacion(new Date());
      conversacion.setConv_ultima_actividad(new Date());
      conversacion.setConv_activa(true);
      conversacion.setConv_oculta_autor(false);
      conversacion.setConv_oculta_interesado(false);
      conversacion.setConv_estado(estado);
      return conversacion;
   }
}

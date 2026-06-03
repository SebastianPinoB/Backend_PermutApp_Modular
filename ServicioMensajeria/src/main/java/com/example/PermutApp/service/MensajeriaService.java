package com.example.PermutApp.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.dto.ConversacionDto;
import com.example.PermutApp.model.dto.MensajeDto;
import com.example.PermutApp.model.dto.PublicacionDto;
import com.example.PermutApp.model.dto.UsuarioDto;
import com.example.PermutApp.model.entities.Conversacion;
import com.example.PermutApp.model.entities.Mensaje;
import com.example.PermutApp.model.request.CrearConversacion;
import com.example.PermutApp.model.request.EnviarMensaje;
import com.example.PermutApp.repository.ConversacionRepository;
import com.example.PermutApp.repository.MensajeRepository;

@Service
public class MensajeriaService {

   private final ConversacionRepository conversacionRepository;
   private final MensajeRepository mensajeRepository;
   private final WebClient usuarioWebClient;
   private final WebClient publicacionWebClient;

   public MensajeriaService(
         ConversacionRepository conversacionRepository,
         MensajeRepository mensajeRepository,
         @Qualifier("usuarioWebClient") WebClient usuarioWebClient,
         @Qualifier("publicacionWebClient") WebClient publicacionWebClient) {
      this.conversacionRepository = conversacionRepository;
      this.mensajeRepository = mensajeRepository;
      this.usuarioWebClient = usuarioWebClient;
      this.publicacionWebClient = publicacionWebClient;
   }

   public ConversacionDto iniciarConversacion(CrearConversacion request, String authorization) {
      validarUsuarioAutenticado(request.getInteresado_id(), authorization);
      PublicacionDto publicacion = obtenerPublicacion(request.getPubl_id());

      if (!Boolean.TRUE.equals(publicacion.publ_activo())) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicacion no esta activa");
      }
      if (publicacion.publ_autor_id() == request.getInteresado_id()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes iniciar una conversacion con tu propia publicacion");
      }

      return conversacionRepository
            .buscarActivaPorPublicacionEInteresado(request.getPubl_id(), request.getInteresado_id())
            .map(conversacion -> convertirConversacion(conversacion, publicacion))
            .orElseGet(() -> crearConversacionNueva(publicacion, request));
   }

   public List<ConversacionDto> listarConversaciones(int usuarioId, String authorization) {
      validarUsuarioAutenticado(usuarioId, authorization);
      return conversacionRepository.listarPorUsuario(usuarioId)
            .stream()
            .map(this::convertirConversacion)
            .toList();
   }

   public ConversacionDto obtenerConversacion(int conversacionId, int usuarioId, String authorization) {
      validarUsuarioAutenticado(usuarioId, authorization);
      Conversacion conversacion = obtenerConversacionPermitida(conversacionId, usuarioId);
      return convertirConversacion(conversacion);
   }

   public List<MensajeDto> listarMensajes(int conversacionId, int usuarioId, String authorization) {
      validarUsuarioAutenticado(usuarioId, authorization);
      obtenerConversacionPermitida(conversacionId, usuarioId);
      return mensajeRepository.listarPorConversacion(conversacionId)
            .stream()
            .map(this::convertirMensaje)
            .toList();
   }

   public MensajeDto enviarMensaje(int conversacionId, EnviarMensaje request, String authorization) {
      validarUsuarioAutenticado(request.getEmisor_id(), authorization);
      Conversacion conversacion = obtenerConversacionPermitida(conversacionId, request.getEmisor_id());
      Mensaje mensaje = guardarMensaje(conversacion.getConv_id(), request.getEmisor_id(), request.getContenido());
      conversacion.setConv_ultima_actividad(mensaje.getMens_fech_envio());
      conversacionRepository.save(conversacion);
      return convertirMensaje(mensaje);
   }

   private ConversacionDto crearConversacionNueva(PublicacionDto publicacion, CrearConversacion request) {
      Date ahora = new Date();
      Conversacion conversacion = new Conversacion();
      conversacion.setPubl_id(publicacion.publ_id());
      conversacion.setPubl_autor_id(publicacion.publ_autor_id());
      conversacion.setInteresado_id(request.getInteresado_id());
      conversacion.setConv_fech_creacion(ahora);
      conversacion.setConv_ultima_actividad(ahora);
      conversacion.setConv_activa(true);
      Conversacion guardada = conversacionRepository.save(conversacion);

      Mensaje inicial = guardarMensaje(guardada.getConv_id(), request.getInteresado_id(), request.getMensaje_inicial());
      guardada.setConv_ultima_actividad(inicial.getMens_fech_envio());
      return convertirConversacion(conversacionRepository.save(guardada), publicacion);
   }

   private Mensaje guardarMensaje(int conversacionId, int emisorId, String contenido) {
      String texto = contenido == null ? "" : contenido.trim();
      if (texto.isBlank()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje no puede estar vacio");
      }
      if (texto.length() > 1000) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje no puede superar 1000 caracteres");
      }

      Mensaje mensaje = new Mensaje();
      mensaje.setConv_id(conversacionId);
      mensaje.setEmisor_id(emisorId);
      mensaje.setMens_contenido(texto);
      mensaje.setMens_fech_envio(new Date());
      return mensajeRepository.save(mensaje);
   }

   private Conversacion obtenerConversacionPermitida(int conversacionId, int usuarioId) {
      Conversacion conversacion = conversacionRepository.findById(conversacionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversacion no encontrada"));
      if (conversacion.getPubl_autor_id() != usuarioId && conversacion.getInteresado_id() != usuarioId) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a esta conversacion");
      }
      return conversacion;
   }

   private UsuarioDto validarUsuarioAutenticado(int usuarioId, String authorization) {
      UsuarioDto usuario;
      try {
         var request = usuarioWebClient.get().uri("/usuario/{idUsuario}", usuarioId);
         if (authorization != null && !authorization.isBlank()) {
            request = request.header("Authorization", authorization);
         }
         usuario = request.retrieve().bodyToMono(UsuarioDto.class).block();
      } catch (Exception e) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }

      if (usuario == null || !Boolean.TRUE.equals(usuario.usu_activo())) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String emailAutenticado = authentication == null ? null : String.valueOf(authentication.getPrincipal());
      if (emailAutenticado == null || !usuario.usu_email().equalsIgnoreCase(emailAutenticado)) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes operar conversaciones de otro usuario");
      }
      return usuario;
   }

   private PublicacionDto obtenerPublicacion(int publicacionId) {
      try {
         return publicacionWebClient.get()
               .uri("/publicacion/{idPublicacion}", publicacionId)
               .retrieve()
               .bodyToMono(PublicacionDto.class)
               .block();
      } catch (Exception e) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada");
      }
   }

   private ConversacionDto convertirConversacion(Conversacion conversacion) {
      PublicacionDto publicacion = obtenerPublicacion(conversacion.getPubl_id());
      return convertirConversacion(conversacion, publicacion);
   }

   private ConversacionDto convertirConversacion(Conversacion conversacion, PublicacionDto publicacion) {
      String ultimoMensaje = mensajeRepository.buscarUltimoPorConversacion(conversacion.getConv_id())
            .map(Mensaje::getMens_contenido)
            .orElse(null);
      return new ConversacionDto(
            conversacion.getConv_id(),
            conversacion.getPubl_id(),
            publicacion == null ? "Publicacion no disponible" : publicacion.publ_titulo(),
            conversacion.getPubl_autor_id(),
            conversacion.getInteresado_id(),
            conversacion.getConv_fech_creacion(),
            conversacion.getConv_ultima_actividad(),
            conversacion.getConv_activa(),
            ultimoMensaje);
   }

   private MensajeDto convertirMensaje(Mensaje mensaje) {
      return new MensajeDto(
            mensaje.getMens_id(),
            mensaje.getConv_id(),
            mensaje.getEmisor_id(),
            mensaje.getMens_contenido(),
            mensaje.getMens_fech_envio());
   }
}

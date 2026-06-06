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
import com.example.PermutApp.model.dto.ProductoDto;
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
   private final WebClient productoWebClient;
   private final NotificacionClient notificacionClient;

   public MensajeriaService(
         ConversacionRepository conversacionRepository,
         MensajeRepository mensajeRepository,
         @Qualifier("usuarioWebClient") WebClient usuarioWebClient,
         @Qualifier("publicacionWebClient") WebClient publicacionWebClient,
         @Qualifier("productoWebClient") WebClient productoWebClient,
         NotificacionClient notificacionClient) {
      this.conversacionRepository = conversacionRepository;
      this.mensajeRepository = mensajeRepository;
      this.usuarioWebClient = usuarioWebClient;
      this.publicacionWebClient = publicacionWebClient;
      this.productoWebClient = productoWebClient;
      this.notificacionClient = notificacionClient;
   }

   public ConversacionDto iniciarConversacion(CrearConversacion request, String authorization) {
      validarUsuarioAutenticado(request.getInteresado_id(), authorization);
      PublicacionDto publicacion = obtenerPublicacion(request.getPubl_id());
      ProductoDto producto = obtenerProducto(request.getProd_id());
      if (publicacion == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicacion no existe o no esta disponible");
      }

      if (!Boolean.TRUE.equals(publicacion.publ_activo())) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicacion no esta activa");
      }
      if (producto == null || producto.publ_id() != publicacion.publ_id()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto no pertenece a la publicacion indicada");
      }
      if (publicacion.publ_autor_id() == request.getInteresado_id()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes iniciar una conversacion con tu propia publicacion");
      }

      return conversacionRepository
            .buscarActivaPorProductoEInteresado(request.getProd_id(), request.getInteresado_id())
            .map(conversacion -> reactivarParaInteresado(conversacion, publicacion))
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
      if (conversacion.getProd_id() == null || obtenerProducto(conversacion.getProd_id()) == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
               "No puedes enviar mensajes porque el producto de este chat ya no esta disponible");
      }
      Mensaje mensaje = guardarMensaje(conversacion.getConv_id(), request.getEmisor_id(), request.getContenido());
      conversacion.setConv_ultima_actividad(mensaje.getMens_fech_envio());
      if (conversacion.getPubl_autor_id() == request.getEmisor_id()) {
         conversacion.setConv_oculta_interesado(false);
      } else {
         conversacion.setConv_oculta_autor(false);
      }
      conversacionRepository.save(conversacion);
      int receptorId = conversacion.getPubl_autor_id() == request.getEmisor_id()
            ? conversacion.getInteresado_id()
            : conversacion.getPubl_autor_id();
      notificacionClient.enviar(
            receptorId,
            "MENSAJE_NUEVO",
            conversacion.getConv_id(),
            conversacion.getPubl_id(),
            null);
      return convertirMensaje(mensaje);
   }

   public void ocultarConversacion(int conversacionId, int usuarioId, String authorization) {
      validarUsuarioAutenticado(usuarioId, authorization);
      Conversacion conversacion = obtenerConversacionPermitida(conversacionId, usuarioId);
      if (conversacion.getPubl_autor_id() == usuarioId) {
         conversacion.setConv_oculta_autor(true);
      } else {
         conversacion.setConv_oculta_interesado(true);
      }
      conversacionRepository.save(conversacion);
   }

   private ConversacionDto crearConversacionNueva(PublicacionDto publicacion, CrearConversacion request) {
      Date ahora = new Date();
      Conversacion conversacion = new Conversacion();
      conversacion.setPubl_id(publicacion.publ_id());
      conversacion.setProd_id(request.getProd_id());
      conversacion.setPubl_autor_id(publicacion.publ_autor_id());
      conversacion.setInteresado_id(request.getInteresado_id());
      conversacion.setConv_fech_creacion(ahora);
      conversacion.setConv_ultima_actividad(ahora);
      conversacion.setConv_activa(true);
      conversacion.setConv_oculta_autor(false);
      conversacion.setConv_oculta_interesado(false);
      Conversacion guardada = conversacionRepository.save(conversacion);

      Mensaje inicial = guardarMensaje(guardada.getConv_id(), request.getInteresado_id(), request.getMensaje_inicial());
      guardada.setConv_ultima_actividad(inicial.getMens_fech_envio());
      Conversacion actualizada = conversacionRepository.save(guardada);
      notificacionClient.enviar(
            actualizada.getPubl_autor_id(),
            "PROPUESTA_PERMUTA",
            actualizada.getConv_id(),
            actualizada.getPubl_id(),
            publicacion.publ_titulo());
      return convertirConversacion(actualizada, publicacion);
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
      boolean estaOculta = conversacion.getPubl_autor_id() == usuarioId
            ? Boolean.TRUE.equals(conversacion.getConv_oculta_autor())
            : Boolean.TRUE.equals(conversacion.getConv_oculta_interesado());
      if (estaOculta) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversacion no encontrada");
      }
      return conversacion;
   }

   private ConversacionDto reactivarParaInteresado(Conversacion conversacion, PublicacionDto publicacion) {
      if (Boolean.TRUE.equals(conversacion.getConv_oculta_interesado())) {
         conversacion.setConv_oculta_interesado(false);
         conversacionRepository.save(conversacion);
      }
      return convertirConversacion(conversacion, publicacion);
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
         return null;
      }
   }

   private ProductoDto obtenerProducto(int productoId) {
      try {
         return productoWebClient.get()
               .uri("/producto/{idProducto}", productoId)
               .retrieve()
               .bodyToMono(ProductoDto.class)
               .block();
      } catch (Exception e) {
         return null;
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
      ProductoDto producto = conversacion.getProd_id() == null
            ? null
            : obtenerProducto(conversacion.getProd_id());
      String titulo = producto == null
            ? "Producto no disponible (chat historico)"
            : publicacion == null ? producto.prod_nombre() : publicacion.publ_titulo();
      return new ConversacionDto(
            conversacion.getConv_id(),
            conversacion.getPubl_id(),
            conversacion.getProd_id(),
            titulo,
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

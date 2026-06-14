package com.example.PermutApp.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.dto.ConversacionDto;
import com.example.PermutApp.model.dto.MensajeDto;
import com.example.PermutApp.model.dto.OfertaProductoDto;
import com.example.PermutApp.model.dto.PublicacionDto;
import com.example.PermutApp.model.dto.ProductoDto;
import com.example.PermutApp.model.dto.ResumenValoracionDto;
import com.example.PermutApp.model.dto.ValoracionDto;
import com.example.PermutApp.model.entities.Conversacion;
import com.example.PermutApp.model.entities.EstadoConversacion;
import com.example.PermutApp.model.entities.Mensaje;
import com.example.PermutApp.model.entities.TipoMensaje;
import com.example.PermutApp.model.entities.Valoracion;
import com.example.PermutApp.model.request.CrearConversacion;
import com.example.PermutApp.model.request.CrearValoracion;
import com.example.PermutApp.model.request.EnviarMensaje;
import com.example.PermutApp.model.request.SeleccionarOferta;
import com.example.PermutApp.repository.ConversacionRepository;
import com.example.PermutApp.repository.MensajeRepository;
import com.example.PermutApp.repository.ValoracionRepository;
import com.example.PermutApp.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MensajeriaService {
   private static final Logger log = LoggerFactory.getLogger(MensajeriaService.class);
   private static final int MENSAJES_REQUERIDOS = 10;

   private final ConversacionRepository conversacionRepository;
   private final MensajeRepository mensajeRepository;
   private final ValoracionRepository valoracionRepository;
   private final WebClient publicacionWebClient;
   private final WebClient productoWebClient;
   private final NotificacionClient notificacionClient;
   private final PublicacionClient publicacionClient;
   private final JwtService jwtService;
   private final ObjectMapper objectMapper;

   public MensajeriaService(
         ConversacionRepository conversacionRepository,
         MensajeRepository mensajeRepository,
         ValoracionRepository valoracionRepository,
         @Qualifier("publicacionWebClient") WebClient publicacionWebClient,
         @Qualifier("productoWebClient") WebClient productoWebClient,
         NotificacionClient notificacionClient,
         PublicacionClient publicacionClient,
         JwtService jwtService,
         ObjectMapper objectMapper) {
      this.conversacionRepository = conversacionRepository;
      this.mensajeRepository = mensajeRepository;
      this.valoracionRepository = valoracionRepository;
      this.publicacionWebClient = publicacionWebClient;
      this.productoWebClient = productoWebClient;
      this.notificacionClient = notificacionClient;
      this.publicacionClient = publicacionClient;
      this.jwtService = jwtService;
      this.objectMapper = objectMapper;
   }

   public ConversacionDto iniciarConversacion(CrearConversacion request, String authorization) {
      validarUsuarioAutenticado(request.getInteresado_id(), authorization);
      PublicacionDto publicacion = obtenerPublicacion(request.getPubl_id());
      ProductoDto producto = obtenerProducto(request.getProd_id());
      if (publicacion == null || !Boolean.TRUE.equals(publicacion.publ_activo())) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicacion no existe o no esta disponible");
      }
      if (producto == null || producto.publ_id() != publicacion.publ_id()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto no pertenece a la publicacion");
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
      return conversacionRepository.listarPorUsuario(usuarioId).stream()
            .map(conversacion -> convertirConversacion(conversacion, usuarioId))
            .toList();
   }

   public ConversacionDto obtenerConversacion(int conversacionId, int usuarioId, String authorization) {
      validarUsuarioAutenticado(usuarioId, authorization);
      return convertirConversacion(obtenerConversacionPermitida(conversacionId, usuarioId), usuarioId);
   }

   public List<MensajeDto> listarMensajes(int conversacionId, int usuarioId, String authorization) {
      validarUsuarioAutenticado(usuarioId, authorization);
      obtenerConversacionPermitida(conversacionId, usuarioId);
      return mensajeRepository.listarPorConversacion(conversacionId).stream()
            .map(this::convertirMensaje)
            .toList();
   }

   @Transactional
   public MensajeDto enviarMensaje(int conversacionId, EnviarMensaje request, String authorization) {
      validarUsuarioAutenticado(request.getEmisor_id(), authorization);
      Conversacion conversacion = obtenerConversacionPermitida(conversacionId, request.getEmisor_id());
      validarNegociacionActiva(conversacion);
      Mensaje mensaje = guardarMensaje(
            conversacion.getConv_id(),
            request.getEmisor_id(),
            request.getContenido(),
            TipoMensaje.TEXTO,
            null);
      actualizarActividadYVisibilidad(conversacion, request.getEmisor_id(), mensaje.getMens_fech_envio());
      notificarOtro(conversacion, request.getEmisor_id(), "MENSAJE_NUEVO");
      return convertirMensaje(mensaje);
   }

   @Transactional
   public ConversacionDto seleccionarOferta(
         int conversacionId,
         SeleccionarOferta request,
         String authorization) {
      validarUsuarioAutenticado(request.usuario_id(), authorization);
      Conversacion conversacion = obtenerConversacionPermitida(conversacionId, request.usuario_id());
      validarNegociacionActiva(conversacion);
      if (conversacion.getInteresado_id() != request.usuario_id()) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo la persona interesada puede seleccionar su oferta");
      }

      ProductoDto producto = obtenerProducto(request.producto_id());
      PublicacionDto publicacion = producto == null ? null : obtenerPublicacion(producto.publ_id());
      if (producto == null || publicacion == null || !Boolean.TRUE.equals(publicacion.publ_activo())) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La permuta ofrecida no esta disponible");
      }
      if (publicacion.publ_autor_id() != request.usuario_id()) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes ofrecer una permuta publicada por ti");
      }
      if (publicacion.publ_id() == conversacion.getPubl_id()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La oferta debe ser distinta al producto original");
      }

      OfertaProductoDto oferta = crearOferta(producto, publicacion);
      conversacion.setProd_ofrecido_id(producto.prod_id());
      conversacion.setPubl_ofrecida_id(publicacion.publ_id());
      Mensaje mensaje = guardarMensaje(
            conversacionId,
            request.usuario_id(),
            "Ofrecio " + producto.prod_nombre() + " para esta permuta.",
            TipoMensaje.OFERTA,
            escribirJson(oferta));
      actualizarActividadYVisibilidad(conversacion, request.usuario_id(), mensaje.getMens_fech_envio());
      notificarOtro(conversacion, request.usuario_id(), "OFERTA_COMPARTIDA");
      return convertirConversacion(conversacion, request.usuario_id());
   }

   @Transactional
   public ConversacionDto solicitarFinalizacion(int conversacionId, int usuarioId, String authorization) {
      validarUsuarioAutenticado(usuarioId, authorization);
      Conversacion conversacion = obtenerConversacionPermitida(conversacionId, usuarioId);
      validarNegociacionActiva(conversacion);
      if (conversacion.getProd_ofrecido_id() == null || conversacion.getPubl_ofrecida_id() == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Primero deben seleccionar el producto ofrecido");
      }
      if (contarMensajes(conversacionId) < MENSAJES_REQUERIDOS) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requieren 10 mensajes para finalizar la permuta");
      }

      conversacion.setConv_estado(EstadoConversacion.FINALIZACION_PENDIENTE);
      conversacion.setFinalizacion_solicitada_por(usuarioId);
      conversacionRepository.save(conversacion);
      guardarMensaje(conversacionId, usuarioId, "Propuso finalizar la permuta.", TipoMensaje.SISTEMA, null);
      notificarOtro(conversacion, usuarioId, "FINALIZACION_SOLICITADA");
      return convertirConversacion(conversacion, usuarioId);
   }

   @Transactional
   public ConversacionDto confirmarFinalizacion(int conversacionId, int usuarioId, String authorization) {
      validarUsuarioAutenticado(usuarioId, authorization);
      Conversacion conversacion = obtenerConversacionPermitida(conversacionId, usuarioId);
      if (estado(conversacion) == EstadoConversacion.FINALIZADA) {
         return convertirConversacion(conversacion, usuarioId);
      }
      if (estado(conversacion) != EstadoConversacion.FINALIZACION_PENDIENTE) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La finalizacion aun no fue solicitada");
      }
      if (conversacion.getFinalizacion_solicitada_por() != null
            && conversacion.getFinalizacion_solicitada_por() == usuarioId) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La otra persona debe confirmar la permuta");
      }
      if (conversacion.getPubl_ofrecida_id() == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La oferta ya no esta disponible");
      }

      publicacionClient.finalizarPermuta(conversacion.getPubl_id(), conversacion.getPubl_ofrecida_id());
      conversacion.setConv_estado(EstadoConversacion.FINALIZADA);
      conversacion.setConv_activa(false);
      conversacion.setConv_fecha_finalizacion(new Date());
      conversacion.setConv_ultima_actividad(new Date());
      conversacionRepository.save(conversacion);
      guardarMensaje(conversacionId, usuarioId, "La permuta fue confirmada por ambas personas.", TipoMensaje.SISTEMA, null);
      notificacionClient.enviar(
            conversacion.getPubl_autor_id(),
            "PERMUTA_FINALIZADA",
            conversacionId,
            conversacion.getPubl_id(),
            null);
      notificacionClient.enviar(
            conversacion.getInteresado_id(),
            "PERMUTA_FINALIZADA",
            conversacionId,
            conversacion.getPubl_id(),
            null);
      return convertirConversacion(conversacion, usuarioId);
   }

   @Transactional
   public ValoracionDto valorar(
         int conversacionId,
         CrearValoracion request,
         String authorization) {
      validarUsuarioAutenticado(request.evaluador_id(), authorization);
      Conversacion conversacion = obtenerConversacionPermitida(conversacionId, request.evaluador_id());
      if (estado(conversacion) != EstadoConversacion.FINALIZADA) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes valorar una permuta finalizada");
      }
      valoracionRepository.buscarPorConversacionYEvaluador(conversacionId, request.evaluador_id())
            .ifPresent(ignored -> {
               throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya valoraste a esta persona");
            });

      int evaluadoId = otroUsuario(conversacion, request.evaluador_id());
      Valoracion valoracion = new Valoracion();
      valoracion.setConv_id(conversacionId);
      valoracion.setEvaluador_id(request.evaluador_id());
      valoracion.setEvaluado_id(evaluadoId);
      valoracion.setVal_estrellas(request.estrellas());
      valoracion.setVal_comentario(request.comentario().trim());
      valoracion.setVal_fecha(new Date());
      Valoracion guardada = valoracionRepository.save(valoracion);
      notificacionClient.enviar(
            evaluadoId,
            "VALORACION_RECIBIDA",
            conversacionId,
            conversacion.getPubl_id(),
            null);
      return convertirValoracion(guardada);
   }

   public ResumenValoracionDto obtenerResumenValoracion(int usuarioId) {
      List<ValoracionDto> valoraciones = valoracionRepository.listarRecibidas(usuarioId).stream()
            .map(this::convertirValoracion)
            .toList();
      double promedio = valoraciones.stream().mapToInt(ValoracionDto::estrellas).average().orElse(0);
      double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;
      return new ResumenValoracionDto(usuarioId, promedioRedondeado, valoraciones.size(), valoraciones);
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
      conversacion.setConv_estado(EstadoConversacion.NEGOCIANDO);
      Conversacion guardada = conversacionRepository.save(conversacion);

      Mensaje inicial = guardarMensaje(
            guardada.getConv_id(),
            request.getInteresado_id(),
            request.getMensaje_inicial(),
            TipoMensaje.TEXTO,
            null);
      guardada.setConv_ultima_actividad(inicial.getMens_fech_envio());
      Conversacion actualizada = conversacionRepository.save(guardada);
      notificacionClient.enviar(
            actualizada.getPubl_autor_id(),
            "PROPUESTA_PERMUTA",
            actualizada.getConv_id(),
            actualizada.getPubl_id(),
            publicacion.publ_titulo());
      return convertirConversacion(actualizada, request.getInteresado_id(), publicacion);
   }

   private Mensaje guardarMensaje(
         int conversacionId,
         int emisorId,
         String contenido,
         TipoMensaje tipo,
         String datos) {
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
      mensaje.setMens_tipo(tipo);
      mensaje.setMens_datos(datos);
      return mensajeRepository.save(mensaje);
   }

   private void actualizarActividadYVisibilidad(Conversacion conversacion, int emisorId, Date fecha) {
      conversacion.setConv_ultima_actividad(fecha);
      if (conversacion.getPubl_autor_id() == emisorId) {
         conversacion.setConv_oculta_interesado(false);
      } else {
         conversacion.setConv_oculta_autor(false);
      }
      conversacionRepository.save(conversacion);
   }

   private void notificarOtro(Conversacion conversacion, int emisorId, String tipo) {
      notificacionClient.enviar(
            otroUsuario(conversacion, emisorId),
            tipo,
            conversacion.getConv_id(),
            conversacion.getPubl_id(),
            null);
   }

   private void validarNegociacionActiva(Conversacion conversacion) {
      if (estado(conversacion) != EstadoConversacion.NEGOCIANDO) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La negociacion ya no admite nuevos mensajes");
      }
      if (conversacion.getProd_id() == null) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto de este chat ya no esta disponible");
      }
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
      return convertirConversacion(conversacion, conversacion.getInteresado_id(), publicacion);
   }

   private void validarUsuarioAutenticado(int usuarioId, String authorization) {
      String token = extraerBearerToken(authorization);
      if (token == null || !jwtService.esTokenValido(token)) {
         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesion no valida");
      }
      int usuarioAutenticadoId;
      try {
         usuarioAutenticadoId = jwtService.obtenerUsuarioId(token);
      } catch (Exception e) {
         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesion no valida");
      }
      if (usuarioAutenticadoId != usuarioId) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes operar conversaciones de otro usuario");
      }
   }

   private String extraerBearerToken(String authorization) {
      return authorization != null && authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : null;
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
         log.warn("No fue posible consultar el producto {}: {}", productoId, e.getMessage());
         return null;
      }
   }

   private ConversacionDto convertirConversacion(Conversacion conversacion, int usuarioId) {
      return convertirConversacion(conversacion, usuarioId, obtenerPublicacion(conversacion.getPubl_id()));
   }

   private ConversacionDto convertirConversacion(
         Conversacion conversacion,
         int usuarioId,
         PublicacionDto publicacion) {
      String ultimoMensaje = mensajeRepository.buscarUltimoPorConversacion(conversacion.getConv_id())
            .map(Mensaje::getMens_contenido)
            .orElse(null);
      ProductoDto producto = conversacion.getProd_id() == null ? null : obtenerProducto(conversacion.getProd_id());
      String titulo = publicacion != null
            ? publicacion.publ_titulo()
            : producto == null ? "Producto no disponible (chat historico)" : producto.prod_nombre();
      int cantidadMensajes = contarMensajes(conversacion.getConv_id());
      EstadoConversacion estado = estado(conversacion);
      boolean yaValoro = valoracionRepository
            .buscarPorConversacionYEvaluador(conversacion.getConv_id(), usuarioId)
            .isPresent();
      boolean tieneOferta = conversacion.getProd_ofrecido_id() != null && conversacion.getPubl_ofrecida_id() != null;
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
            ultimoMensaje,
            estado,
            cantidadMensajes,
            Math.max(0, MENSAJES_REQUERIDOS - cantidadMensajes),
            conversacion.getFinalizacion_solicitada_por(),
            conversacion.getConv_fecha_finalizacion(),
            obtenerOfertaActual(conversacion),
            otroUsuario(conversacion, usuarioId),
            estado == EstadoConversacion.NEGOCIANDO && usuarioId == conversacion.getInteresado_id(),
            estado == EstadoConversacion.NEGOCIANDO && tieneOferta && cantidadMensajes >= MENSAJES_REQUERIDOS,
            estado == EstadoConversacion.FINALIZACION_PENDIENTE
                  && conversacion.getFinalizacion_solicitada_por() != null
                  && conversacion.getFinalizacion_solicitada_por() != usuarioId,
            estado == EstadoConversacion.FINALIZADA && !yaValoro,
            yaValoro);
   }

   private MensajeDto convertirMensaje(Mensaje mensaje) {
      TipoMensaje tipo = mensaje.getMens_tipo() == null ? TipoMensaje.TEXTO : mensaje.getMens_tipo();
      OfertaProductoDto oferta = null;
      if (tipo == TipoMensaje.OFERTA && mensaje.getMens_datos() != null) {
         try {
            oferta = objectMapper.readValue(mensaje.getMens_datos(), OfertaProductoDto.class);
         } catch (Exception e) {
            log.warn("Mensaje de oferta {} contiene datos invalidos", mensaje.getMens_id());
         }
      }
      return new MensajeDto(
            mensaje.getMens_id(),
            mensaje.getConv_id(),
            mensaje.getEmisor_id(),
            mensaje.getMens_contenido(),
            mensaje.getMens_fech_envio(),
            tipo,
            oferta);
   }

   private OfertaProductoDto obtenerOfertaActual(Conversacion conversacion) {
      if (conversacion.getProd_ofrecido_id() == null || conversacion.getPubl_ofrecida_id() == null) {
         return null;
      }
      ProductoDto producto = obtenerProducto(conversacion.getProd_ofrecido_id());
      PublicacionDto publicacion = obtenerPublicacion(conversacion.getPubl_ofrecida_id());
      return producto == null || publicacion == null ? null : crearOferta(producto, publicacion);
   }

   private OfertaProductoDto crearOferta(ProductoDto producto, PublicacionDto publicacion) {
      String imagen = producto.prod_imagenes() == null || producto.prod_imagenes().isEmpty()
            ? null
            : producto.prod_imagenes().get(0);
      return new OfertaProductoDto(
            producto.prod_id(),
            publicacion.publ_id(),
            producto.prod_nombre(),
            publicacion.publ_titulo(),
            producto.prod_est(),
            producto.prod_precio(),
            imagen);
   }

   private int contarMensajes(int conversacionId) {
      return Math.toIntExact(mensajeRepository.contarMensajesUsuario(conversacionId));
   }

   private int otroUsuario(Conversacion conversacion, int usuarioId) {
      return conversacion.getPubl_autor_id() == usuarioId
            ? conversacion.getInteresado_id()
            : conversacion.getPubl_autor_id();
   }

   private EstadoConversacion estado(Conversacion conversacion) {
      return conversacion.getConv_estado() == null
            ? EstadoConversacion.NEGOCIANDO
            : conversacion.getConv_estado();
   }

   private ValoracionDto convertirValoracion(Valoracion valoracion) {
      return new ValoracionDto(
            valoracion.getVal_id(),
            valoracion.getConv_id(),
            valoracion.getEvaluador_id(),
            valoracion.getEvaluado_id(),
            valoracion.getVal_estrellas(),
            valoracion.getVal_comentario(),
            valoracion.getVal_fecha());
   }

   private String escribirJson(Object value) {
      try {
         return objectMapper.writeValueAsString(value);
      } catch (Exception e) {
         throw new IllegalStateException("No fue posible serializar la oferta", e);
      }
   }
}

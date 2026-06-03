package com.example.PermutApp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PermutApp.model.dto.ConversacionDto;
import com.example.PermutApp.model.dto.MensajeDto;
import com.example.PermutApp.model.request.CrearConversacion;
import com.example.PermutApp.model.request.EnviarMensaje;
import com.example.PermutApp.service.MensajeriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("chat")
public class MensajeriaController {

   private final MensajeriaService mensajeriaService;

   public MensajeriaController(MensajeriaService mensajeriaService) {
      this.mensajeriaService = mensajeriaService;
   }

   @PostMapping("/conversaciones")
   public ConversacionDto iniciarConversacion(
         @Valid @RequestBody CrearConversacion request,
         @RequestHeader(value = "Authorization", required = false) String authorization) {
      return mensajeriaService.iniciarConversacion(request, authorization);
   }

   @GetMapping("/conversaciones/usuario/{usuarioId}")
   public List<ConversacionDto> listarConversaciones(
         @PathVariable Integer usuarioId,
         @RequestHeader(value = "Authorization", required = false) String authorization) {
      return mensajeriaService.listarConversaciones(usuarioId, authorization);
   }

   @GetMapping("/conversaciones/{conversacionId}")
   public ConversacionDto obtenerConversacion(
         @PathVariable Integer conversacionId,
         @RequestParam Integer usuarioId,
         @RequestHeader(value = "Authorization", required = false) String authorization) {
      return mensajeriaService.obtenerConversacion(conversacionId, usuarioId, authorization);
   }

   @GetMapping("/conversaciones/{conversacionId}/mensajes")
   public List<MensajeDto> listarMensajes(
         @PathVariable Integer conversacionId,
         @RequestParam Integer usuarioId,
         @RequestHeader(value = "Authorization", required = false) String authorization) {
      return mensajeriaService.listarMensajes(conversacionId, usuarioId, authorization);
   }

   @PostMapping("/conversaciones/{conversacionId}/mensajes")
   public MensajeDto enviarMensaje(
         @PathVariable Integer conversacionId,
         @Valid @RequestBody EnviarMensaje request,
         @RequestHeader(value = "Authorization", required = false) String authorization) {
      return mensajeriaService.enviarMensaje(conversacionId, request, authorization);
   }
}

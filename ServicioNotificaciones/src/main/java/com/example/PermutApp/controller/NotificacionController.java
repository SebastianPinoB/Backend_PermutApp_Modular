package com.example.PermutApp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.dto.ConteoNoLeidasDto;
import com.example.PermutApp.model.dto.NotificacionDto;
import com.example.PermutApp.model.dto.SuscripcionDto;
import com.example.PermutApp.model.request.CrearEventoNotificacionRequest;
import com.example.PermutApp.model.request.RegistrarSuscripcionRequest;
import com.example.PermutApp.service.NotificacionService;

import jakarta.validation.Valid;

@RestController
public class NotificacionController {
   private final NotificacionService service;
   private final String internalApiKey;
   private final String vapidPublicKey;

   public NotificacionController(
         NotificacionService service,
         @Value("${notifications.internal-api-key:change-me-internal-notification-key}") String internalApiKey,
         @Value("${notifications.vapid.public-key:}") String vapidPublicKey) {
      this.service = service;
      this.internalApiKey = internalApiKey;
      this.vapidPublicKey = vapidPublicKey;
   }

   @PostMapping("/notificaciones/suscripciones")
   public SuscripcionDto registrar(@Valid @RequestBody RegistrarSuscripcionRequest request) {
      return service.registrarSuscripcion(usuarioAutenticado(), request);
   }

   @DeleteMapping("/notificaciones/suscripciones/{id}")
   public void desactivar(@PathVariable int id) {
      service.desactivarSuscripcion(usuarioAutenticado(), id);
   }

   @GetMapping("/notificaciones")
   public List<NotificacionDto> listar() {
      return service.listar(usuarioAutenticado());
   }

   @GetMapping("/notificaciones/no-leidas")
   public ConteoNoLeidasDto contarNoLeidas() {
      return new ConteoNoLeidasDto(service.contarNoLeidas(usuarioAutenticado()));
   }

   @PatchMapping("/notificaciones/{id}/leer")
   public void marcarLeida(@PathVariable int id) {
      service.marcarLeida(usuarioAutenticado(), id);
   }

   @PostMapping("/notificaciones/leer-todas")
   public void marcarTodasLeidas() {
      service.marcarTodasLeidas(usuarioAutenticado());
   }

   @GetMapping("/notificaciones/vapid-public-key")
   public Map<String, String> vapidPublicKey() {
      return Map.of("publicKey", vapidPublicKey);
   }

   @PostMapping("/internal/notificaciones/eventos")
   public NotificacionDto crearEvento(
         @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey,
         @Valid @RequestBody CrearEventoNotificacionRequest request) {
      if (apiKey == null || !internalApiKey.equals(apiKey)) {
         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Clave interna invalida");
      }
      return service.crearEvento(request);
   }

   private int usuarioAutenticado() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || !(authentication.getPrincipal() instanceof Integer usuarioId)) {
         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
      }
      return usuarioId;
   }
}

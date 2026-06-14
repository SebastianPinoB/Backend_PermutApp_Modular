package com.example.PermutApp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.request.FinalizarPermutaRequest;
import com.example.PermutApp.service.PublicacionService;

import jakarta.validation.Valid;

@RestController
public class InternalPublicacionController {
   private final PublicacionService publicacionService;
   private final String internalApiKey;

   public InternalPublicacionController(
         PublicacionService publicacionService,
         @Value("${services.publicacion.internal-api-key:change-me-internal-notification-key}") String internalApiKey) {
      this.publicacionService = publicacionService;
      this.internalApiKey = internalApiKey;
   }

   @PostMapping("/internal/publicaciones/finalizar-permuta")
   public void finalizarPermuta(
         @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey,
         @Valid @RequestBody FinalizarPermutaRequest request) {
      if (apiKey == null || !internalApiKey.equals(apiKey)) {
         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Clave interna invalida");
      }
      publicacionService.finalizarPermuta(request.publicacionAutorId(), request.publicacionOfrecidaId());
   }
}

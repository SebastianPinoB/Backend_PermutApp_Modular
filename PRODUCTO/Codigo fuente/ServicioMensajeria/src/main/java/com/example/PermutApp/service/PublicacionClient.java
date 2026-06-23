package com.example.PermutApp.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PublicacionClient {
   private static final Logger log = LoggerFactory.getLogger(PublicacionClient.class);
   private final WebClient webClient;
   private final String internalApiKey;

   public PublicacionClient(
         @Qualifier("publicacionWebClient") WebClient webClient,
         @Value("${services.publicacion.internal-api-key:change-me-internal-notification-key}") String internalApiKey) {
      this.webClient = webClient;
      this.internalApiKey = internalApiKey;
   }

   public void finalizarPermuta(int publicacionAutorId, int publicacionOfrecidaId) {
      try {
         webClient.post()
               .uri("/internal/publicaciones/finalizar-permuta")
               .header("X-Internal-Api-Key", internalApiKey)
               .bodyValue(Map.of(
                     "publicacionAutorId", publicacionAutorId,
                     "publicacionOfrecidaId", publicacionOfrecidaId))
               .retrieve()
               .toBodilessEntity()
               .block();
         log.info("Publicaciones {} y {} retiradas por permuta", publicacionAutorId, publicacionOfrecidaId);
      } catch (Exception e) {
         log.error(
               "Fallo al retirar publicaciones {} y {}: {}",
               publicacionAutorId,
               publicacionOfrecidaId,
               e.getMessage());
         throw new ResponseStatusException(
               HttpStatus.BAD_GATEWAY,
               "No fue posible retirar las publicaciones de la permuta");
      }
   }
}

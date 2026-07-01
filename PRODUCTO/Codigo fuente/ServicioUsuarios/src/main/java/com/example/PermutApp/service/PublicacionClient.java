package com.example.PermutApp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PublicacionClient {
   private final WebClient webClient;
   private final String internalApiKey;

   public PublicacionClient(
         @Value("${services.publicacion.base-url:http://127.0.0.1:6001}") String baseUrl,
         @Value("${services.publicacion.internal-api-key:change-me-internal-notification-key}") String internalApiKey) {
      this.webClient = WebClient.builder().baseUrl(baseUrl).build();
      this.internalApiKey = internalApiKey;
   }

   public void desactivarPorUsuario(int usuarioId) {
      try {
         webClient.post()
               .uri("/internal/publicaciones/desactivar-usuario/{usuarioId}", usuarioId)
               .header("X-Internal-Api-Key", internalApiKey)
               .retrieve()
               .toBodilessEntity()
               .block();
      } catch (Exception exception) {
         throw new ResponseStatusException(
               HttpStatus.BAD_GATEWAY,
               "No fue posible retirar las publicaciones de la cuenta");
      }
   }
}

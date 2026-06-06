package com.example.PermutApp.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class NotificacionClient {
   private static final Logger LOGGER = LoggerFactory.getLogger(NotificacionClient.class);

   private final WebClient webClient;
   private final String internalApiKey;

   public NotificacionClient(
         @Value("${services.notificacion.base-url:http://127.0.0.1:7002}") String baseUrl,
         @Value("${services.notificacion.internal-api-key:change-me-internal-notification-key}") String internalApiKey) {
      this.webClient = WebClient.builder().baseUrl(baseUrl).build();
      this.internalApiKey = internalApiKey;
   }

   public void enviarEstadoIdentidad(int usuarioId, String tipo) {
      webClient.post()
            .uri("/internal/notificaciones/eventos")
            .header("X-Internal-Api-Key", internalApiKey)
            .bodyValue(Map.of("usuarioId", usuarioId, "tipo", tipo))
            .retrieve()
            .toBodilessEntity()
            .subscribe(
                  ignored -> { },
                  error -> LOGGER.warn("No fue posible emitir el evento de identidad {}: {}", tipo, error.getMessage()));
   }
}

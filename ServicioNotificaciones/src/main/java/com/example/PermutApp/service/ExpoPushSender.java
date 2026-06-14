package com.example.PermutApp.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ExpoPushSender {
   private static final Logger log = LoggerFactory.getLogger(ExpoPushSender.class);
   private final WebClient webClient;

   public ExpoPushSender(@Value("${notifications.expo.url:https://exp.host/--/api/v2/push/send}") String expoPushUrl) {
      this.webClient = WebClient.builder().baseUrl(expoPushUrl).build();
   }

   public ResultadoEnvioPush enviar(String token, String titulo, String cuerpo, Map<String, Object> datos) {
      try {
         Map<?, ?> response = webClient.post()
               .bodyValue(Map.of(
                     "to", token,
                     "title", titulo,
                     "body", cuerpo,
                     "data", datos,
                     "sound", "default",
                     "priority", "high"))
               .retrieve()
               .bodyToMono(Map.class)
               .block();
         Object dataValue = response == null ? null : response.get("data");
         Map<?, ?> ticket = dataValue instanceof Map<?, ?> map ? map : null;
         if (ticket != null && "ok".equals(ticket.get("status"))) {
            return ResultadoEnvioPush.ENVIADA;
         }
         Object detailsValue = ticket == null ? null : ticket.get("details");
         Map<?, ?> details = detailsValue instanceof Map<?, ?> map ? map : null;
         String error = details == null ? null : String.valueOf(details.get("error"));
         if ("DeviceNotRegistered".equals(error)) {
            return ResultadoEnvioPush.SUSCRIPCION_INVALIDA;
         }
         log.warn("Expo rechazo el push para {}: {}", ocultar(token), response);
         return ResultadoEnvioPush.ERROR_TEMPORAL;
      } catch (Exception e) {
         log.warn("Error enviando push Expo a {}: {}", ocultar(token), e.getMessage());
         return ResultadoEnvioPush.ERROR_TEMPORAL;
      }
   }

   private String ocultar(String token) {
      return token == null || token.length() < 12 ? "***" : token.substring(0, 8) + "...";
   }
}

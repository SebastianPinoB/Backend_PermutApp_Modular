package com.example.PermutApp.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ExpoPushSender {
   private final WebClient webClient;

   public ExpoPushSender(@Value("${notifications.expo.url:https://exp.host/--/api/v2/push/send}") String expoPushUrl) {
      this.webClient = WebClient.builder().baseUrl(expoPushUrl).build();
   }

   public boolean enviar(String token, String titulo, String cuerpo, Map<String, Object> datos) {
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
         return response != null;
      } catch (Exception e) {
         return false;
      }
   }
}

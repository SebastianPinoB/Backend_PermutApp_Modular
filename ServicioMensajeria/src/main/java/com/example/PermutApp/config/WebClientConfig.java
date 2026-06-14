package com.example.PermutApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

   @Bean
   public WebClient usuarioWebClient(@Value("${services.usuario.base-url:http://127.0.0.1:5001}") String usuarioBaseUrl) {
      return WebClient.builder().baseUrl(usuarioBaseUrl).build();
   }

   @Bean
   public WebClient publicacionWebClient(@Value("${services.publicacion.base-url:http://127.0.0.1:6000}") String publicacionBaseUrl) {
      return WebClient.builder().baseUrl(publicacionBaseUrl).build();
   }

   @Bean
   public WebClient productoWebClient(
         @Value("${services.producto.base-url:http://127.0.0.1:5050}") String productoBaseUrl,
         @Value("${services.producto.max-in-memory-size-bytes:10485760}") int maxInMemorySizeBytes) {
      return WebClient.builder()
            .baseUrl(productoBaseUrl)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySizeBytes))
            .build();
   }
}

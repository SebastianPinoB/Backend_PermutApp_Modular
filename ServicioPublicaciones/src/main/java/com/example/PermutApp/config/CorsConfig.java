package com.example.PermutApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

   private final String[] allowedOrigins;

   public CorsConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
      this.allowedOrigins = allowedOrigins.split(",");
   }

   @Override
   public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")
            .allowedOrigins(trimmedOrigins())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Content-Type")
            .allowCredentials(true);
   }

   private String[] trimmedOrigins() {
      for (int index = 0; index < allowedOrigins.length; index++) {
         allowedOrigins[index] = allowedOrigins[index].trim();
      }
      return allowedOrigins;
   }
}

package com.example.PermutApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class LegacyJacksonConfig {
   @Bean
   public ObjectMapper legacyObjectMapper() {
      return new ObjectMapper();
   }
}

package com.example.PermutApp.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

   @GetMapping("/")
   public Map<String, Object> inicio() {
      return Map.of(
            "service", "ServicioUsuarios",
            "status", "ok",
            "auth", Map.of(
                  "register", "POST /auth/register",
                  "login", "POST /auth/login"));
   }
}

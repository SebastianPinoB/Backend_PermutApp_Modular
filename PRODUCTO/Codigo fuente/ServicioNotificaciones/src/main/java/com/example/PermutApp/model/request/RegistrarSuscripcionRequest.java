package com.example.PermutApp.model.request;

import com.example.PermutApp.model.entities.CanalNotificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarSuscripcionRequest(
      @NotNull CanalNotificacion canal,
      @NotBlank String destino,
      String p256dh,
      String auth,
      @NotBlank String plataforma) {
}

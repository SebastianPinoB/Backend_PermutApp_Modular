package com.example.PermutApp.model.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
      @NotBlank String recoveryToken,
      @NotBlank
      @Size(min = 8, message = "La nueva contrasena debe tener al menos 8 caracteres")
      @Pattern(
            regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*",
            message = "La nueva contrasena debe incluir al menos un caracter especial")
      String newPassword) {
}

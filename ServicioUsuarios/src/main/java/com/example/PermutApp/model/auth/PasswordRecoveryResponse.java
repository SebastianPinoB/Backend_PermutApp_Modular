package com.example.PermutApp.model.auth;

public record PasswordRecoveryResponse(
      String recoveryToken,
      long expiresIn,
      String message) {
}

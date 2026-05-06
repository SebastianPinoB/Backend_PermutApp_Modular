package com.example.PermutApp.model.auth;

import com.example.PermutApp.model.dto.UsuarioDto;

public record AuthResponse(
      String token,
      String tokenType,
      long expiresIn,
      UsuarioDto usuario) {
}

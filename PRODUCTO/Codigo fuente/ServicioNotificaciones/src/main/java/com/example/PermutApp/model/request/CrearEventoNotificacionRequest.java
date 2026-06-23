package com.example.PermutApp.model.request;

import com.example.PermutApp.model.entities.TipoNotificacion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearEventoNotificacionRequest(
      @Positive int usuarioId,
      @NotNull TipoNotificacion tipo,
      Integer conversacionId,
      Integer publicacionId,
      String publicacionTitulo) {
}

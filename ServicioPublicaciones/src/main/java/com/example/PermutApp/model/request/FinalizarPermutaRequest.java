package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FinalizarPermutaRequest(
      @NotNull @Positive Integer publicacionAutorId,
      @NotNull @Positive Integer publicacionOfrecidaId) {
}

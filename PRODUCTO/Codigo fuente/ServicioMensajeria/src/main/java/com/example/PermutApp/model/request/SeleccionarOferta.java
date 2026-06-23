package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SeleccionarOferta(
      @NotNull @Positive Integer usuario_id,
      @NotNull @Positive Integer producto_id) {
}

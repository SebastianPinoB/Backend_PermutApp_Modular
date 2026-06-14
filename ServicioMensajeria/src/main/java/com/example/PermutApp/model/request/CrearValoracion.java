package com.example.PermutApp.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearValoracion(
      @NotNull @Positive Integer evaluador_id,
      @NotNull @Min(1) @Max(5) Integer estrellas,
      @NotBlank @Size(max = 300) String comentario) {
}

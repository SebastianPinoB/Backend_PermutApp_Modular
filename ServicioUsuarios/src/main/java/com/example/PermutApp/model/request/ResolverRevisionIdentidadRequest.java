package com.example.PermutApp.model.request;

import com.example.PermutApp.model.Entities.VerificacionIdentidad.EstadoVerificacion;

import jakarta.validation.constraints.NotNull;

public record ResolverRevisionIdentidadRequest(
      @NotNull EstadoVerificacion estado,
      String observacion) {
}

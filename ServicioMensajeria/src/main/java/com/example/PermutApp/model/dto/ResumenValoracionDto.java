package com.example.PermutApp.model.dto;

import java.util.List;

public record ResumenValoracionDto(
      int usuario_id,
      double promedio,
      long cantidad,
      List<ValoracionDto> valoraciones) {
}

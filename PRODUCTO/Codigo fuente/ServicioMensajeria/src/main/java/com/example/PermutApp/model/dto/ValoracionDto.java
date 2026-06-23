package com.example.PermutApp.model.dto;

import java.util.Date;

public record ValoracionDto(
      int val_id,
      int conv_id,
      int evaluador_id,
      int evaluado_id,
      int estrellas,
      String comentario,
      Date fecha) {
}

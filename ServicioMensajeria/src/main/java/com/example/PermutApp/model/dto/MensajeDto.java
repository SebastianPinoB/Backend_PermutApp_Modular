package com.example.PermutApp.model.dto;

import java.util.Date;

public record MensajeDto(
      int mens_id,
      int conv_id,
      int emisor_id,
      String mens_contenido,
      Date mens_fech_envio) {
}

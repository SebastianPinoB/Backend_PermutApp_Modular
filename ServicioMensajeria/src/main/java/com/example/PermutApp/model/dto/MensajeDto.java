package com.example.PermutApp.model.dto;

import java.util.Date;

import com.example.PermutApp.model.entities.TipoMensaje;

public record MensajeDto(
      int mens_id,
      int conv_id,
      int emisor_id,
      String mens_contenido,
      Date mens_fech_envio,
      TipoMensaje mens_tipo,
      OfertaProductoDto oferta) {
}

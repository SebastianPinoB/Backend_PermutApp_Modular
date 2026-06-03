package com.example.PermutApp.model.dto;

import java.util.Date;

public record ConversacionDto(
      int conv_id,
      int publ_id,
      String publ_titulo,
      int publ_autor_id,
      int interesado_id,
      Date conv_fech_creacion,
      Date conv_ultima_actividad,
      Boolean conv_activa,
      String ultimo_mensaje) {
}

package com.example.PermutApp.model.dto;

import java.util.Date;

import com.example.PermutApp.model.entities.EstadoConversacion;

public record ConversacionDto(
      int conv_id,
      int publ_id,
      Integer prod_id,
      String publ_titulo,
      int publ_autor_id,
      int interesado_id,
      Date conv_fech_creacion,
      Date conv_ultima_actividad,
      Boolean conv_activa,
      String ultimo_mensaje,
      EstadoConversacion conv_estado,
      int cantidad_mensajes,
      int mensajes_para_finalizar,
      Integer finalizacion_solicitada_por,
      Date conv_fecha_finalizacion,
      OfertaProductoDto oferta,
      int otro_usuario_id,
      boolean puede_ofertar,
      boolean puede_solicitar_finalizacion,
      boolean puede_confirmar_finalizacion,
      boolean puede_valorar,
      boolean usuario_ya_valoro) {
}

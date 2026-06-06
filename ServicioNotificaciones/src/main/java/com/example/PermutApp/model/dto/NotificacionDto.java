package com.example.PermutApp.model.dto;

import java.time.Instant;
import java.util.Map;

import com.example.PermutApp.model.entities.TipoNotificacion;

public record NotificacionDto(
      int notif_id,
      TipoNotificacion notif_tipo,
      String notif_titulo,
      String notif_cuerpo,
      Map<String, Object> notif_datos,
      Instant notif_fecha,
      boolean notif_leida) {
}

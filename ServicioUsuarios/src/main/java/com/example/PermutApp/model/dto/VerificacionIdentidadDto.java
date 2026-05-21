package com.example.PermutApp.model.dto;

import java.time.Instant;

import com.example.PermutApp.model.Entities.VerificacionIdentidad.EstadoVerificacion;

public record VerificacionIdentidadDto(
      int ver_id,
      int usu_id,
      EstadoVerificacion ver_estado,
      Double ver_face_similarity,
      Double ver_face_threshold,
      String ver_face_provider,
      String ver_ocr_run_detectado,
      String ver_ocr_nombre_detectado,
      Boolean ver_run_match,
      String ver_ocr_provider,
      Instant ver_fecha,
      String ver_observacion) {
}

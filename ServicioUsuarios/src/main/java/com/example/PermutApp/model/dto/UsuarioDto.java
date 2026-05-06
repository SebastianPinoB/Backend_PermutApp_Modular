package com.example.PermutApp.model.dto;

public record UsuarioDto(
      int usu_id,
      int usu_numrun,
      Character usu_dvrun,
      String usu_pri_nombre,
      String usu_seg_nombre,
      String usu_pri_apellido,
      String usu_seg_apellido,
      String usu_email,
      int usu_prom_rep,
      boolean usu_activo) {
}

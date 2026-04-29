package com.example.PermutApp.model.dto;

public record UsuarioDto(
   Boolean usu_activo,
   char usu_dvrun,
   String usu_email,
   int usu_id,
   int usu_numrun,
   String usu_pass, 
   String usu_pri_apellido,
   String usu_pri_nombre,
   int usu_prom_rep,
   String usu_seg_apellido,
   String usu_seg_nombre
){
   
}

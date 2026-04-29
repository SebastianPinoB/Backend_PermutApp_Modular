package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrearUsuario {
   @NotBlank
   private int usu_numrun;
   @NotBlank
   private char usu_dvrun;
   @NotBlank
   private String usu_pri_nombre;
   private String usu_seg_nombre;
   @NotBlank
   private String usu_pri_apellido;
   private String usu_seg_apellido;
   @NotBlank
   private String usu_email;
   @NotBlank
   private String usu_pass;
   @NotBlank
   private int usu_prom_rep;
   @NotBlank
   private boolean usu_activo;
}

package com.example.PermutApp.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarUsuario {

   @NotNull
   private Integer usu_id;
   @NotNull
   @Positive
   private Integer usu_numrun;
   @NotNull
   private Character usu_dvrun;
   @NotBlank
   @Size(max = 15)
   private String usu_pri_nombre;
   @Size(max = 15)
   private String usu_seg_nombre;
   @NotBlank
   @Size(max = 15)
   private String usu_pri_apellido;
   @Size(max = 15)
   private String usu_seg_apellido;
   @NotBlank
   @Email
   @Size(max = 25)
   private String usu_email;
   @Size(min = 8)
   private String usu_pass;
   private Integer usu_prom_rep;
   private Boolean usu_activo;
}

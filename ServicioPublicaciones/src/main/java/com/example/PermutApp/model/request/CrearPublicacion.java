package com.example.PermutApp.model.request;


import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrearPublicacion {

   @NotBlank
   private String publ_titulo;
   @NotBlank
   private String publ_descripcion;
   @NotBlank
   private Date publ_fech_creacion;
   @NotBlank
   private Boolean publ_activo;
   @NotBlank
   private int usu_id;
   
}

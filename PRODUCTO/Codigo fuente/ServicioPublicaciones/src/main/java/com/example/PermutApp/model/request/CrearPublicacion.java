package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearPublicacion {

   @NotBlank
   private String publ_titulo;

   @NotBlank
   private String publ_descripcion;

   @NotNull
   @Positive
   private Integer usu_id;
}

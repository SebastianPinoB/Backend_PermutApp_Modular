package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearPublicacion {

   @NotBlank
   @Size(max = 22, message = "El titulo puede tener hasta 22 caracteres")
   private String publ_titulo;

   @NotBlank
   @Size(max = 100, message = "La descripcion puede tener hasta 100 caracteres")
   private String publ_descripcion;

   @NotNull
   @Positive
   private Integer usu_id;
}

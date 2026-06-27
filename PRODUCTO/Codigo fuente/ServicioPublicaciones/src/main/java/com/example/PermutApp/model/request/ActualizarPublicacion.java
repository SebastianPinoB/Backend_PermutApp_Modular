package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarPublicacion {
   @NotBlank
   @Size(max = 80, message = "El titulo puede tener hasta 80 caracteres")
   private String publ_titulo;
   @NotBlank
   @Size(max = 500, message = "La descripcion puede tener hasta 500 caracteres")
   private String publ_descripcion;
}

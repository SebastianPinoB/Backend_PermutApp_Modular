package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActualizarPublicacion {
   @NotBlank
   private String publ_titulo;
   @NotBlank
   private String publ_descripcion;
}

package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearConversacion {

   @NotNull
   @Positive
   private Integer publ_id;

   @NotNull
   @Positive
   private Integer prod_id;

   @NotNull
   @Positive
   private Integer interesado_id;

   @NotBlank
   @Size(max = 1000)
   private String mensaje_inicial;
}

package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnviarMensaje {

   @NotNull
   @Positive
   private Integer emisor_id;

   @NotBlank
   @Size(max = 1000)
   private String contenido;
}

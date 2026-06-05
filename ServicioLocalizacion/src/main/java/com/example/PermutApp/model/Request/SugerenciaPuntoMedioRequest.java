package com.example.PermutApp.model.Request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SugerenciaPuntoMedioRequest {
   @NotNull
   @DecimalMin("-90.0")
   @DecimalMax("90.0")
   private Double latitudOrigen;

   @NotNull
   @DecimalMin("-180.0")
   @DecimalMax("180.0")
   private Double longitudOrigen;

   @NotNull
   @DecimalMin("-90.0")
   @DecimalMax("90.0")
   private Double latitudDestino;

   @NotNull
   @DecimalMin("-180.0")
   @DecimalMax("180.0")
   private Double longitudDestino;
}

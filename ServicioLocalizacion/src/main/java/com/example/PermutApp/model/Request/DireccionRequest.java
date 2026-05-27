package com.example.PermutApp.model.Request;

import lombok.Data;

@Data
public class DireccionRequest {
   private String nombreCalle;
   private String addLetra;
   private Integer numero;
   private Integer comunaId;
}

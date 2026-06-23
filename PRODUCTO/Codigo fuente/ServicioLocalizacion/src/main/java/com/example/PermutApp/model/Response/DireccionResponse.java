package com.example.PermutApp.model.Response;

import lombok.Data;

@Data
public class DireccionResponse {
   private Integer id;
   private String nombreCalle;
   private String addLetra;
   private Integer numero;
   private Integer comunaId;
   private String nombreComuna;
}

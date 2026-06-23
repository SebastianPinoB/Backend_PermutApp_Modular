package com.example.PermutApp.model.Response;

import lombok.Data;

@Data
public class EstacionMetroResponse {
   private Integer id;
   private String nombre;
   private String linea;
   private Integer orden;
   private Boolean esCombinacion;
   private Double latitud;
   private Double longitud;
   private String direccion;
   private String comuna;
}

package com.example.PermutApp.model.Response;

import lombok.Data;

@Data
public class PuntoEncuentroResponse {
   private Integer id;
   private Double latitud;
   private Double longitud;
   private String referencias;
   private Integer comunaId;
   private String nombreComuna;
}

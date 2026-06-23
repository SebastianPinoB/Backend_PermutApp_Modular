package com.example.PermutApp.model.Request;

import lombok.Data;

@Data
public class PuntoEncuentroRequest {
   private Double latitud;
   private Double longitud;
   private String referencias;
   private Integer comunaId;
}

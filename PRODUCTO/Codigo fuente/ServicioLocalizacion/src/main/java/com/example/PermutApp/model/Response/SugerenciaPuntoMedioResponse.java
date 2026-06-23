package com.example.PermutApp.model.Response;

import lombok.Data;

@Data
public class SugerenciaPuntoMedioResponse {
   private Double puntoMedioLatitud;
   private Double puntoMedioLongitud;
   private EstacionMetroResponse estacionSugerida;
   private Double distanciaPuntoMedioKm;
   private Double distanciaOrigenKm;
   private Double distanciaDestinoKm;
   private String criterio;
}

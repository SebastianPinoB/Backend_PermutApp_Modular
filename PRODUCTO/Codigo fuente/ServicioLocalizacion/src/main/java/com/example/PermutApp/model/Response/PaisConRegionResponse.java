package com.example.PermutApp.model.Response;

import java.util.List;

import lombok.Data;

@Data
public class PaisConRegionResponse {
   private Integer id;
   private String nombre;
   private List<RegionResponse> regiones;
}

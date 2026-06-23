package com.example.PermutApp.model.dto;

public record OfertaProductoDto(
      int prod_id,
      int publ_id,
      String nombre,
      String titulo,
      String estado,
      int precio,
      String imagen) {
}

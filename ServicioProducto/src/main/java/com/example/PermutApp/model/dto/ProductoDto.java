package com.example.PermutApp.model.dto;

import java.util.List;

public record ProductoDto(
    int prod_id,
    String prod_nombre,
    String prod_est,
    int prod_precio,
    int publ_id,
    List<String> prod_imagenes
){

}

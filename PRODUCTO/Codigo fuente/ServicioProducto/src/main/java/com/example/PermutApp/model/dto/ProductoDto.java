package com.example.PermutApp.model.dto;

import java.util.List;

public record ProductoDto(
    int prod_id,
    String prod_nombre,
    String prod_est,
    String prod_categoria,
    int prod_precio,
    int publ_id,
    List<String> prod_imagenes,
    String prod_ubicacion_comuna,
    String prod_ubicacion_referencia,
    Double prod_latitud_aprox,
    Double prod_longitud_aprox
){

}

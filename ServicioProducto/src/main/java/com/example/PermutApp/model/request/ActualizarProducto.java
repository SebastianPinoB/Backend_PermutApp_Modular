package com.example.PermutApp.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarProducto {
    @NotBlank
    private String prod_nombre;
    @NotBlank
    private String prod_est;
    private String prod_categoria;
    @NotNull
    @Min(0)
    private Integer prod_precio;
    private String prod_ubicacion_comuna;
    private String prod_ubicacion_referencia;
    private Double prod_latitud_aprox;
    private Double prod_longitud_aprox;
}

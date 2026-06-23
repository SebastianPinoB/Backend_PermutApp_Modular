package com.example.PermutApp.model.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearProducto {
    @NotBlank
    private String prod_nombre;
    @NotBlank
    private String prod_est;
    private String prod_categoria;
    @NotNull
    @Min(0)
    private Integer prod_precio;
    @NotNull
    @Min(1)
    private Integer publ_id;
    @Size(max = 5)
    private List<String> prod_imagenes;
    private String prod_ubicacion_comuna;
    private String prod_ubicacion_referencia;
    private Double prod_latitud_aprox;
    private Double prod_longitud_aprox;
}

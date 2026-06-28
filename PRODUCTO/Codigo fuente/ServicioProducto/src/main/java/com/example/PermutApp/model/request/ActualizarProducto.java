package com.example.PermutApp.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarProducto {
    @NotBlank
    @Size(max = 20, message = "El nombre puede tener hasta 20 caracteres")
    private String prod_nombre;
    @NotBlank
    @Size(max = 20, message = "El estado puede tener hasta 20 caracteres")
    private String prod_est;
    @Size(max = 40, message = "La categoria puede tener hasta 40 caracteres")
    private String prod_categoria;
    @NotNull
    @Min(0)
    @Max(value = 5000000, message = "El valor referencial maximo es 5000000")
    private Integer prod_precio;
    @Size(max = 60, message = "La comuna puede tener hasta 60 caracteres")
    private String prod_ubicacion_comuna;
    @Size(max = 50, message = "La referencia puede tener hasta 50 caracteres")
    private String prod_ubicacion_referencia;
    private Double prod_latitud_aprox;
    private Double prod_longitud_aprox;
}

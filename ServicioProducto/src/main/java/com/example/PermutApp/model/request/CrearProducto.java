package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrearProducto {
    @NotBlank
    private String prod_nombre;
    @NotBlank
    private String prod_est;
    @NotBlank
    private int prod_precio;
}

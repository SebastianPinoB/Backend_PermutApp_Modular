package com.example.PermutApp.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearProducto {
    @NotBlank
    private String prod_nombre;
    @NotBlank
    private String prod_est;
    @NotNull
    @Min(0)
    private Integer prod_precio;
    @NotNull
    @Min(1)
    private Integer publ_id;
}

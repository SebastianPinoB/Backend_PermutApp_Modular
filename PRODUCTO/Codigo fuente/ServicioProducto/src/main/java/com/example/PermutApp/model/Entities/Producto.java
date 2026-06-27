package com.example.PermutApp.model.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int prod_id;
    @Column(nullable = false, length = 60)
    private String prod_nombre;
    @Column(nullable = false, length = 20)
    private String prod_est;
    @Column(length = 40)
    private String prod_categoria;
    @Column(nullable = false)
    private int prod_precio;
    @Column(nullable = false)
    private int publ_id;
    @Column(length = 60)
    private String prod_ubicacion_comuna;
    @Column(length = 120)
    private String prod_ubicacion_referencia;
    private Double prod_latitud_aprox;
    private Double prod_longitud_aprox;

}

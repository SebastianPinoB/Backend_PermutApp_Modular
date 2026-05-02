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
    @Column(nullable = false)
    private String prod_nombre;
    @Column(nullable = false)
    private String prod_est;
    @Column(nullable = false)
    private int prod_precio;
    @Column(nullable = false)
    private int publ_id;

}

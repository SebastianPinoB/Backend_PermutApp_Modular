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
@Table(name = "producto_imagen")
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int prod_img_id;

    @Column(nullable = false)
    private int prod_id;

    @Column(nullable = false)
    private int prod_img_orden;

    @Column(nullable = false, columnDefinition = "text")
    private String prod_img_url;
}

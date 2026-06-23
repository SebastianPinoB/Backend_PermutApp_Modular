package com.example.PermutApp.model.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "producto_categoria")
@Data
public class ProductoCategoria {
    @Id
    @Column(nullable = false, length = 40)
    private String cat_id;
    @Column(nullable = false)
    private String cat_nombre;
    @Column(nullable = false)
    private String cat_query;
    @Column(nullable = false)
    private String cat_icon;
    @Column(nullable = false)
    private String cat_bg_color;
    @Column(nullable = false)
    private String cat_border_color;
    @Column(nullable = false)
    private String cat_icon_color;
    @Column(columnDefinition = "text")
    private String cat_keywords;
    @Column(nullable = false)
    private int cat_orden;
    @Column(nullable = false)
    private boolean cat_activa = true;
}

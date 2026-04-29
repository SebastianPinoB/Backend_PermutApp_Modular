package com.example.PermutApp.model.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "publicacion")
public class Publicacion {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int publ_id;
   @Column(nullable = false)
   private String publ_titulo;
   @Column(nullable = false)
   private String publ_descripcion;
   @Column(nullable = false)
   private Date publ_fech_creacion;
   @Column(nullable = false)
   private Boolean publ_activo;
   @Column(nullable = false)
   private int publ_autor_id;

}

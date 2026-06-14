package com.example.PermutApp.model.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(
      name = "mensajeria_valoracion",
      uniqueConstraints = @UniqueConstraint(
            name = "uk_valoracion_conversacion_evaluador",
            columnNames = { "conv_id", "evaluador_id" }))
public class Valoracion {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int val_id;
   @Column(nullable = false)
   private int conv_id;
   @Column(nullable = false)
   private int evaluador_id;
   @Column(nullable = false)
   private int evaluado_id;
   @Column(nullable = false)
   private int val_estrellas;
   @Column(nullable = false, length = 300)
   private String val_comentario;
   @Column(nullable = false)
   private Date val_fecha;
}

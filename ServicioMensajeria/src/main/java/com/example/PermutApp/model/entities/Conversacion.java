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
@Table(name = "mensajeria_conversacion")
public class Conversacion {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int conv_id;

   @Column(nullable = false)
   private int publ_id;

   @Column(nullable = false)
   private int publ_autor_id;

   @Column(nullable = false)
   private int interesado_id;

   @Column(nullable = false)
   private Date conv_fech_creacion;

   @Column(nullable = false)
   private Date conv_ultima_actividad;

   @Column(nullable = false)
   private Boolean conv_activa;
}

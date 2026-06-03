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
@Table(name = "mensajeria_mensaje")
public class Mensaje {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int mens_id;

   @Column(nullable = false)
   private int conv_id;

   @Column(nullable = false)
   private int emisor_id;

   @Column(nullable = false, length = 1000)
   private String mens_contenido;

   @Column(nullable = false)
   private Date mens_fech_envio;
}

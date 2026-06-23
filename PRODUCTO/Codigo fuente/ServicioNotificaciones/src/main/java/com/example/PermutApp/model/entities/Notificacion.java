package com.example.PermutApp.model.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "notificacion")
public class Notificacion {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int notif_id;

   @Column(nullable = false)
   private int usu_id;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false, length = 40)
   private TipoNotificacion notif_tipo;

   @Column(nullable = false, length = 120)
   private String notif_titulo;

   @Column(nullable = false, length = 400)
   private String notif_cuerpo;

   @Column(nullable = false, columnDefinition = "text")
   private String notif_datos;

   @Column(nullable = false)
   private Instant notif_fecha;

   @Column(nullable = false)
   private boolean notif_leida;
}

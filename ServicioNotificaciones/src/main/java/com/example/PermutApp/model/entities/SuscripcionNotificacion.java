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
@Table(name = "notificacion_suscripcion")
public class SuscripcionNotificacion {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int nsus_id;

   @Column(nullable = false)
   private int usu_id;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false, length = 16)
   private CanalNotificacion nsus_canal;

   @Column(nullable = false, unique = true, length = 2048)
   private String nsus_destino;

   @Column(length = 512)
   private String nsus_p256dh;

   @Column(length = 256)
   private String nsus_auth;

   @Column(nullable = false, length = 32)
   private String nsus_plataforma;

   @Column(nullable = false)
   private boolean nsus_activa;

   @Column(nullable = false)
   private Instant nsus_fecha_creacion;

   @Column(nullable = false)
   private Instant nsus_fecha_actualizacion;
}

package com.example.PermutApp.model.Entities;

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
@Table(name = "verificacion_identidad")
public class VerificacionIdentidad {

   public enum EstadoVerificacion {
      PENDIENTE,
      APROBADA,
      RECHAZADA,
      REVISION_MANUAL
   }

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int ver_id;

   @Column(nullable = false)
   private int usu_id;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private EstadoVerificacion ver_estado;

   @Column(nullable = false)
   private Double ver_face_similarity;

   @Column(nullable = false)
   private Double ver_face_threshold;

   @Column(nullable = false)
   private String ver_face_provider;

   @Column(nullable = true)
   private String ver_ocr_run_detectado;

   @Column(nullable = true)
   private String ver_ocr_nombre_detectado;

   @Column(nullable = false)
   private Boolean ver_run_match;

   @Column(nullable = true)
   private Boolean ver_nombre_match;

   @Column(nullable = false)
   private String ver_ocr_provider;

   @Column(nullable = false)
   private Instant ver_fecha;

   @Column(nullable = true, length = 500)
   private String ver_observacion;

   @Column(nullable = true)
   private String ver_revisor_email;

   @Column(nullable = true)
   private Instant ver_fecha_revision;
}

package com.example.PermutApp.model.Entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "password_recovery_token")
public class PasswordRecoveryToken {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long prt_id;

   @Column(nullable = false)
   private int usu_id;

   @Column(nullable = false, unique = true, length = 64)
   private String prt_token_hash;

   @Column(nullable = false)
   private Instant prt_expires_at;

   @Column(nullable = false)
   private boolean prt_used;

   @Column(nullable = false)
   private Instant prt_created_at;

   @Column(nullable = true)
   private Instant prt_used_at;
}

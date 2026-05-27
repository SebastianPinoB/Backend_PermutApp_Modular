package com.example.PermutApp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "direccion")
public class Direccion {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int addId;

   private String addCalle;
   private int addNumero;
   private String addLetra;

   @ManyToOne
   @JoinColumn(name = "comu_id")
   private Comuna comuna;
}

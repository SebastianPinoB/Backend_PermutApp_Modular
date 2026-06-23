package com.example.PermutApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
@Table(name = "comuna")
public class Comuna {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int comuId;
   private String comuNombre;

   @ManyToOne
   @JoinColumn(name = "ciu_id", nullable = false)
   @JsonBackReference
   private Ciudad ciudad;
}
package com.example.PermutApp.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "pais")
public class Pais {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int paisId;
   private String paisNombre;

   @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL)
   @JsonManagedReference
   private List<Region> regiones;
}

package com.example.PermutApp.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "region")
public class Region {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int regiId;
   private String regiNombre;

   @ManyToOne
   @JoinColumn(name = "pais_id", nullable = false)
   @JsonBackReference
   private Pais pais;

   @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
   @JsonManagedReference
   private List<Ciudad> ciudades;
}

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
@Table(name = "puntoEncuentro")
public class PuntoEncuentro {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int sugIdPuntoEncuentro;

   private Double latitud;
   private Double longitud;
   private String referencias;

   @ManyToOne
   @JoinColumn(name = "comu_id") // Asegúrate de que el name coincida con la columna en tu BD
   private Comuna comuna;

}

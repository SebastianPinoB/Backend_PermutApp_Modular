package com.example.PermutApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PermutApp.model.entities.Publicacion;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer>{
   
}

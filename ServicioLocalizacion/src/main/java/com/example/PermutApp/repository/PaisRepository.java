package com.example.PermutApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.PermutApp.model.Pais;


@Repository
public interface PaisRepository extends JpaRepository<Pais, Integer>{
   boolean existsByPaisNombreIgnoreCase(String nombre);
   Optional<Pais> findByPaisNombreIgnoreCase(String nombre);
}

package com.example.PermutApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.PermutApp.model.EstacionMetro;

@Repository
public interface EstacionMetroRepository extends JpaRepository<EstacionMetro, Integer> {
   List<EstacionMetro> findByLinea(String linea);
   List<EstacionMetro> findByLatitudIsNotNullAndLongitudIsNotNull();
   boolean existsByNombreIgnoreCaseAndLineaIgnoreCase(String nombre, String linea);
}

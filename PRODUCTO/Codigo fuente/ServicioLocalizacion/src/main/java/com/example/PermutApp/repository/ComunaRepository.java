package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.PermutApp.model.Comuna;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna, Integer>{
   List<Comuna> findByCiudadCiudadId(Integer ciudadId);
   @Query("SELECT COUNT(c) > 0 FROM Comuna c WHERE LOWER(c.comuNombre) = LOWER(:nombre) AND c.ciudad.ciudadId = :ciudadId")
boolean existsByNombreYCiudad(@Param("nombre") String nombre, @Param("ciudadId") Integer ciudadId);

@Query("SELECT c FROM Comuna c WHERE LOWER(c.comuNombre) = LOWER(:nombre) AND c.ciudad.ciudadId = :ciudadId")
Optional<Comuna> findByNombreYCiudad(@Param("nombre") String nombre, @Param("ciudadId") Integer ciudadId);
}

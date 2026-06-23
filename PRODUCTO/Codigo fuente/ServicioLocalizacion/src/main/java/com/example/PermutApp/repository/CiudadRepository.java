package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.PermutApp.model.Ciudad;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {
   List<Ciudad> findByRegionRegiId(Integer regionId);

   @Query("SELECT COUNT(c) > 0 FROM Ciudad c WHERE LOWER(c.ciudadNombre) = LOWER(:nombre) AND c.region.regiId = :regionId")
   boolean existsByNombreYRegion(@Param("nombre") String nombre, @Param("regionId") Integer regionId);

   @Query("SELECT c FROM Ciudad c WHERE LOWER(c.ciudadNombre) = LOWER(:nombre) AND c.region.regiId = :regionId")
   Optional<Ciudad> findByNombreYRegion(@Param("nombre") String nombre, @Param("regionId") Integer regionId);
}

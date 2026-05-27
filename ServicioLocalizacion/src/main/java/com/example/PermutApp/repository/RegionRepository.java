package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.PermutApp.model.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
   List<Region> findByPaisPaisId(Integer paisId);

   @Query("SELECT COUNT(r) > 0 FROM Region r WHERE LOWER(r.regiNombre) = LOWER(:nombre) AND r.pais.paisId = :paisId")
   boolean existsByNombreYPais(@Param("nombre") String nombre, @Param("paisId") Integer paisId);

   @Query("SELECT r FROM Region r WHERE LOWER(r.regiNombre) = LOWER(:nombre) AND r.pais.paisId = :paisId")
   Optional<Region> findByNombreYPais(@Param("nombre") String nombre, @Param("paisId") Integer paisId);
}

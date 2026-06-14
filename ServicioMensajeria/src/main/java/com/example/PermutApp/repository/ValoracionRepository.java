package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.entities.Valoracion;

public interface ValoracionRepository extends JpaRepository<Valoracion, Integer> {
   @Query(value = "select * from mensajeria_valoracion where conv_id = :convId and evaluador_id = :evaluadorId limit 1", nativeQuery = true)
   Optional<Valoracion> buscarPorConversacionYEvaluador(
         @Param("convId") int conversacionId,
         @Param("evaluadorId") int evaluadorId);

   @Query(value = "select * from mensajeria_valoracion where evaluado_id = :usuarioId order by val_fecha desc", nativeQuery = true)
   List<Valoracion> listarRecibidas(@Param("usuarioId") int usuarioId);
}

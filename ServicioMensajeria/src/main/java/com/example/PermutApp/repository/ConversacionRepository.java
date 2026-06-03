package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.entities.Conversacion;

public interface ConversacionRepository extends JpaRepository<Conversacion, Integer> {

   @Query(value = "select * from mensajeria_conversacion where publ_id = :publId and interesado_id = :interesadoId and conv_activa = true limit 1", nativeQuery = true)
   Optional<Conversacion> buscarActivaPorPublicacionEInteresado(@Param("publId") int publId,
         @Param("interesadoId") int interesadoId);

   @Query(value = "select * from mensajeria_conversacion where publ_autor_id = :usuarioId or interesado_id = :usuarioId order by conv_ultima_actividad desc", nativeQuery = true)
   List<Conversacion> listarPorUsuario(@Param("usuarioId") int usuarioId);
}

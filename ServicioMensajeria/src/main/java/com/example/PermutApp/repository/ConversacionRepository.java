package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.entities.Conversacion;

public interface ConversacionRepository extends JpaRepository<Conversacion, Integer> {

   @Query(value = "select * from mensajeria_conversacion where prod_id = :productoId and interesado_id = :interesadoId and conv_activa = true limit 1", nativeQuery = true)
   Optional<Conversacion> buscarActivaPorProductoEInteresado(@Param("productoId") int productoId,
         @Param("interesadoId") int interesadoId);

   @Query(value = """
         select *
           from mensajeria_conversacion
          where (publ_autor_id = :usuarioId and coalesce(conv_oculta_autor, false) = false)
             or (interesado_id = :usuarioId and coalesce(conv_oculta_interesado, false) = false)
          order by conv_ultima_actividad desc
         """, nativeQuery = true)
   List<Conversacion> listarPorUsuario(@Param("usuarioId") int usuarioId);
}

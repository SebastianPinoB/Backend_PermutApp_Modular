package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.entities.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {
   @Query(value = "select * from mensajeria_mensaje where conv_id = :convId order by mens_fech_envio asc", nativeQuery = true)
   List<Mensaje> listarPorConversacion(@Param("convId") int convId);

   @Query(value = "select * from mensajeria_mensaje where conv_id = :convId order by mens_fech_envio desc limit 1", nativeQuery = true)
   Optional<Mensaje> buscarUltimoPorConversacion(@Param("convId") int convId);

   @Query(value = "select count(*) from mensajeria_mensaje where conv_id = :convId and coalesce(mens_tipo, 'TEXTO') <> 'SISTEMA'", nativeQuery = true)
   long contarMensajesUsuario(@Param("convId") int convId);
}

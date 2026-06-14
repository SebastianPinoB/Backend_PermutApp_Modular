package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.PermutApp.model.entities.SuscripcionNotificacion;

public interface SuscripcionNotificacionRepository extends JpaRepository<SuscripcionNotificacion, Integer> {
   @Query(value = "select * from notificacion_suscripcion where nsus_destino = :destino limit 1", nativeQuery = true)
   Optional<SuscripcionNotificacion> buscarPorDestino(@Param("destino") String destino);

   @Query(value = "select * from notificacion_suscripcion where usu_id = :usuarioId and nsus_activa = true", nativeQuery = true)
   List<SuscripcionNotificacion> listarActivasPorUsuario(@Param("usuarioId") int usuarioId);

   @Modifying
   @Transactional
   @Query(value = "update notificacion_suscripcion set nsus_activa = false, nsus_fecha_actualizacion = now() where nsus_id = :id and usu_id = :usuarioId", nativeQuery = true)
   int desactivar(@Param("id") int id, @Param("usuarioId") int usuarioId);

   @Modifying
   @Transactional
   @Query(value = "update notificacion_suscripcion set nsus_activa = false, nsus_fecha_actualizacion = now() where nsus_id = :id", nativeQuery = true)
   int desactivarPorId(@Param("id") int id);
}

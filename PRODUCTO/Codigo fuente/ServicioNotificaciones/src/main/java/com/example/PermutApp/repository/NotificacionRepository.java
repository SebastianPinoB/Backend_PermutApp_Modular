package com.example.PermutApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.PermutApp.model.entities.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
   @Query(value = "select * from notificacion where usu_id = :usuarioId order by notif_fecha desc limit 100", nativeQuery = true)
   List<Notificacion> listarPorUsuario(@Param("usuarioId") int usuarioId);

   @Query(value = "select count(*) from notificacion where usu_id = :usuarioId and notif_leida = false", nativeQuery = true)
   long contarNoLeidas(@Param("usuarioId") int usuarioId);

   @Modifying
   @Transactional
   @Query(value = "update notificacion set notif_leida = true where notif_id = :id and usu_id = :usuarioId", nativeQuery = true)
   int marcarLeida(@Param("id") int id, @Param("usuarioId") int usuarioId);

   @Modifying
   @Transactional
   @Query(value = "update notificacion set notif_leida = true where usu_id = :usuarioId and notif_leida = false", nativeQuery = true)
   int marcarTodasLeidas(@Param("usuarioId") int usuarioId);

   @Modifying
   @Transactional
   @Query(value = "delete from notificacion where notif_id = :id and usu_id = :usuarioId", nativeQuery = true)
   int eliminar(@Param("id") int id, @Param("usuarioId") int usuarioId);
}

package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.Entities.VerificacionIdentidad;
public interface VerificacionIdentidadRepository extends JpaRepository<VerificacionIdentidad, Integer> {

   @Query(value = "select * from verificacion_identidad where usu_id = :usuId order by ver_fecha desc limit 1", nativeQuery = true)
   Optional<VerificacionIdentidad> findUltimaByUsuario(@Param("usuId") int usuId);

   @Query(value = "select * from verificacion_identidad where ver_estado = :estado order by ver_fecha asc", nativeQuery = true)
   List<VerificacionIdentidad> findRevisionManual(@Param("estado") String estado);

   @Modifying
   @Query(value = "delete from verificacion_identidad where usu_id = :usuId", nativeQuery = true)
   int eliminarPorUsuario(@Param("usuId") int usuId);
}

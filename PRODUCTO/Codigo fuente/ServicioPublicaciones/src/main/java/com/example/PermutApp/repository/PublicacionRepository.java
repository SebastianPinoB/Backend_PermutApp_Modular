package com.example.PermutApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.PermutApp.model.entities.Publicacion;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {
   @Query(value = "select * from publicacion where publ_activo = true order by publ_fech_creacion desc", nativeQuery = true)
   List<Publicacion> listarActivas();
}

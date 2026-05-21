package com.example.PermutApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.Entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

   @Query("select u from Usuario u where lower(u.usu_email) = lower(:usuEmail)")
   Optional<Usuario> findByUsuEmailIgnoreCase(@Param("usuEmail") String usuEmail);

   @Query("select count(u) > 0 from Usuario u where lower(u.usu_email) = lower(:usuEmail)")
   boolean existsByUsuEmailIgnoreCase(@Param("usuEmail") String usuEmail);
}

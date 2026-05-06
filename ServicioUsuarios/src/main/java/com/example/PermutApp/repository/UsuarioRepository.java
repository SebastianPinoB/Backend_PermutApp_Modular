package com.example.PermutApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.Entities.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

   @Query("SELECT u FROM Usuario u WHERE LOWER(u.usu_email) = LOWER(:usuEmail)")
   Optional<Usuario> findByUsuEmailIgnoreCase(@Param("usuEmail") String usuEmail);

   @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE LOWER(u.usu_email) = LOWER(:usuEmail)")
   boolean existsByUsuEmailIgnoreCase(@Param("usuEmail") String usuEmail);
}

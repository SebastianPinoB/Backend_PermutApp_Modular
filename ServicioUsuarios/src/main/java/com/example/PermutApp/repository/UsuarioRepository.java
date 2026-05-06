package com.example.PermutApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PermutApp.model.Entities.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

   Optional<Usuario> findByUsuEmailIgnoreCase(String usuEmail);

   boolean existsByUsuEmailIgnoreCase(String usuEmail);
}

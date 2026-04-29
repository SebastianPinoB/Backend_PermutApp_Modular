package com.example.PermutApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PermutApp.model.Entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
   
}

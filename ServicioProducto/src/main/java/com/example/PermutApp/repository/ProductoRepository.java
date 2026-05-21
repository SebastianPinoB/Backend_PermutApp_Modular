package com.example.PermutApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PermutApp.model.Entities.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer>{
    
}

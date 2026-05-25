package com.example.PermutApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.PermutApp.model.Direccion;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer>{
   
}

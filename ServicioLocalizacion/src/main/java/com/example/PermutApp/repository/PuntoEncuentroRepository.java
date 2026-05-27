package com.example.PermutApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.PermutApp.model.PuntoEncuentro;

@Repository
public interface PuntoEncuentroRepository extends JpaRepository<PuntoEncuentro, Integer>{
   List<PuntoEncuentro> findByComunaComuId(Integer comunaId);
}

package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.Entities.ProductoCategoria;

public interface ProductoCategoriaRepository extends JpaRepository<ProductoCategoria, String> {
    @Query(value = "select * from producto_categoria where cat_activa = true order by cat_orden asc, cat_nombre asc", nativeQuery = true)
    List<ProductoCategoria> findActivasOrdenadas();

    @Query(value = "select * from producto_categoria where cat_id = :id and cat_activa = true", nativeQuery = true)
    Optional<ProductoCategoria> findActivaPorId(@Param("id") String id);
}

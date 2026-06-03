package com.example.PermutApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.Entities.ProductoImagen;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Integer> {
    @Query(value = "select * from producto_imagen where prod_id = :prodId order by prod_img_orden asc", nativeQuery = true)
    List<ProductoImagen> findByProductoIdOrderByOrden(@Param("prodId") int prodId);

    @Modifying
    @Query(value = "delete from producto_imagen where prod_id = :prodId", nativeQuery = true)
    void deleteByProductoId(@Param("prodId") int prodId);
}

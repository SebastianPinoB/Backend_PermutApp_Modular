package com.example.PermutApp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PermutApp.model.dto.ProductoDto;
import com.example.PermutApp.model.request.ActualizarProducto;
import com.example.PermutApp.model.request.CrearProducto;
import com.example.PermutApp.service.ProductoService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("producto")
public class ProductoController {

    @Autowired
    ProductoService productoService;

    @PostMapping("")
    public ProductoDto crearProducto(@RequestBody CrearProducto nuevo) {
        return productoService.crearProducto(nuevo);
    }

    @GetMapping("")
    public List<ProductoDto> obtenerTodosProductos() {
        return productoService.obtenerTodos();
    }

    @GetMapping("/{idProducto}")
    public ProductoDto buscarPorIdProducto(@PathVariable Integer idProducto) {
        return productoService.obtenerPorId(idProducto);
    }

    @PutMapping("/{idProducto}")
    public ProductoDto modificarProducto(@PathVariable Integer idProducto, @RequestBody ActualizarProducto nuevo) {
        return productoService.modificarProducto(idProducto, nuevo);
    }

    @DeleteMapping("/{idProducto}")
    public String eliminarProducto(@PathVariable Integer idProducto) {
        return productoService.eliminarProducto(idProducto);
    }

}

package com.example.PermutApp.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Producto;
import com.example.PermutApp.model.request.ActualizarProducto;
import com.example.PermutApp.model.request.CrearProducto;
import com.example.PermutApp.repository.ProductoRepository;

public class ProductoService {

    private ProductoRepository productoRepository;

    // Probar
    public Producto crearProducto(CrearProducto nuevo) {
        if (nuevo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No es posible crear el producto");
        } else {
            Producto producto = new Producto();
            producto.setProd_nombre(nuevo.getProd_nombre());
            producto.setProd_est(nuevo.getProd_est());
            producto.setProd_precio(nuevo.getProd_precio());

            return productoRepository.save(producto);
        }

    }

    // Probar
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // Probar
    public Producto obtenerPorId(int idProducto){
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if(producto == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        return producto;
    }


    //Probar
    public Producto modificarProducto(Integer idProducto, ActualizarProducto nuevo){
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if(producto == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }else{
            producto.setProd_nombre(nuevo.getProd_nombre());
            producto.setProd_est(nuevo.getProd_est());
            producto.setProd_precio(nuevo.getProd_precio());

            return productoRepository.save(producto);
        }
    }

    //Probar
    public String eliminarProducto(int idProducto){
        if(productoRepository.existsById(idProducto)){
            productoRepository.deleteById(idProducto);
            return "Producto eliminado correctamente";
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }

    }

}
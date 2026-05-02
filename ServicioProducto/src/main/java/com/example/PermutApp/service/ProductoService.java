package com.example.PermutApp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Producto;
import com.example.PermutApp.model.dto.ProductoDto;
import com.example.PermutApp.model.dto.PublicacionDto;
import com.example.PermutApp.model.request.ActualizarProducto;
import com.example.PermutApp.model.request.CrearProducto;
import com.example.PermutApp.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private WebClient publicacionWebClient;

    @Autowired
    private ProductoRepository productoRepository;

    // Convierte entidad a DTO de respuesta
    private ProductoDto convertirADto(Producto producto) {
        return new ProductoDto(
            producto.getProd_id(),
            producto.getProd_nombre(),
            producto.getProd_est(),
            producto.getProd_precio(),
            producto.getPubl_id()
        );
    }

    public ProductoDto crearProducto(CrearProducto nuevo) {
        if (nuevo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No es posible crear el producto");
        }

        // Verificar que la publicacion exista via WebClient
        PublicacionDto publicacion = null;
        try {
            publicacion = publicacionWebClient.get()
                .uri("/publicacion/{idPublicacion}", nuevo.getPubl_id())
                .retrieve()
                .bodyToMono(PublicacionDto.class)
                .block();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ENDPOINT ---> Publicacion no encontrada");
        }

        if (publicacion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada");
        }

        Producto producto = new Producto();
        producto.setProd_nombre(nuevo.getProd_nombre());
        producto.setProd_est(nuevo.getProd_est());
        producto.setProd_precio(nuevo.getProd_precio());
        producto.setPubl_id(nuevo.getPubl_id());

        return convertirADto(productoRepository.save(producto));
    }

    public List<ProductoDto> obtenerTodos() {
        return productoRepository.findAll()
            .stream()
            .map(this::convertirADto)
            .toList();
    }

    public ProductoDto obtenerPorId(int idProducto) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        return convertirADto(producto);
    }

    public ProductoDto modificarProducto(Integer idProducto, ActualizarProducto nuevo) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        } else {
            producto.setProd_nombre(nuevo.getProd_nombre());
            producto.setProd_est(nuevo.getProd_est());
            producto.setProd_precio(nuevo.getProd_precio());

            return convertirADto(productoRepository.save(producto));
        }
    }

    public String eliminarProducto(int idProducto) {
        if (productoRepository.existsById(idProducto)) {
            productoRepository.deleteById(idProducto);
            return "Producto eliminado correctamente";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
    }

}
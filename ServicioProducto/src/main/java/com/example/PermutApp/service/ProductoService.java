package com.example.PermutApp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Producto;
import com.example.PermutApp.model.Entities.ProductoImagen;
import com.example.PermutApp.model.dto.ProductoDto;
import com.example.PermutApp.model.dto.PublicacionDto;
import com.example.PermutApp.model.request.ActualizarProducto;
import com.example.PermutApp.model.request.CrearProducto;
import com.example.PermutApp.repository.ProductoImagenRepository;
import com.example.PermutApp.repository.ProductoRepository;

@Service
public class ProductoService {

    private static final int MAX_IMAGENES_PRODUCTO = 5;

    @Autowired
    private WebClient publicacionWebClient;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoImagenRepository productoImagenRepository;

    private ProductoDto convertirADto(Producto producto) {
        List<String> imagenes = productoImagenRepository.findByProductoIdOrderByOrden(producto.getProd_id())
            .stream()
            .map(ProductoImagen::getProd_img_url)
            .toList();

        return new ProductoDto(
            producto.getProd_id(),
            producto.getProd_nombre(),
            producto.getProd_est(),
            producto.getProd_precio(),
            producto.getPubl_id(),
            imagenes
        );
    }

    @Transactional
    public ProductoDto crearProducto(CrearProducto nuevo) {
        if (nuevo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No es posible crear el producto");
        }

        List<String> imagenes = limpiarImagenes(nuevo.getProd_imagenes());

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

        Producto guardado = productoRepository.save(producto);
        guardarImagenes(guardado.getProd_id(), imagenes);
        return convertirADto(guardado);
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

    @Transactional
    public String eliminarProducto(int idProducto) {
        if (productoRepository.existsById(idProducto)) {
            productoImagenRepository.deleteByProductoId(idProducto);
            productoRepository.deleteById(idProducto);
            return "Producto eliminado correctamente";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
    }

    private List<String> limpiarImagenes(List<String> imagenes) {
        if (imagenes == null) {
            return List.of();
        }

        List<String> limpias = imagenes.stream()
            .filter(imagen -> imagen != null && !imagen.isBlank())
            .map(String::trim)
            .toList();

        if (limpias.size() > MAX_IMAGENES_PRODUCTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un producto puede tener maximo 5 fotos");
        }

        return limpias;
    }

    private void guardarImagenes(int productoId, List<String> imagenes) {
        for (int i = 0; i < imagenes.size(); i++) {
            ProductoImagen imagen = new ProductoImagen();
            imagen.setProd_id(productoId);
            imagen.setProd_img_orden(i + 1);
            imagen.setProd_img_url(imagenes.get(i));
            productoImagenRepository.save(imagen);
        }
    }
}

package com.example.PermutApp.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Producto;
import com.example.PermutApp.model.Entities.ProductoCategoria;
import com.example.PermutApp.model.Entities.ProductoImagen;
import com.example.PermutApp.model.dto.ProductoCategoriaDto;
import com.example.PermutApp.model.dto.ProductoDto;
import com.example.PermutApp.model.dto.PublicacionDto;
import com.example.PermutApp.model.request.ActualizarProducto;
import com.example.PermutApp.model.request.CrearProducto;
import com.example.PermutApp.repository.ProductoCategoriaRepository;
import com.example.PermutApp.repository.ProductoImagenRepository;
import com.example.PermutApp.repository.ProductoRepository;

@Service
public class ProductoService {

    private static final int MAX_IMAGENES_PRODUCTO = 5;
    private static final List<CategoriaBase> CATEGORIAS_BASE = List.of(
        new CategoriaBase("electronica", "Electrónica", "electronica", "laptop", "#f0f9ff", "#bae6fd", "#0284c7", List.of("electronica", "notebook", "celular", "tablet", "monitor", "pc", "computador", "audifono"), 1),
        new CategoriaBase("deportes", "Deportes", "deportes", "futbol-o", "#fff7ed", "#fed7aa", "#ea580c", List.of("deporte", "bicicleta", "pelota", "futbol", "zapatilla", "raqueta", "mancuerna"), 2),
        new CategoriaBase("hogar", "Hogar", "hogar", "home", "#f5f3ff", "#ddd6fe", "#7c3aed", List.of("hogar", "cocina", "decoracion", "electrodomestico", "lampara"), 3),
        new CategoriaBase("moda", "Moda", "moda", "shopping-bag", "#fff1f2", "#fecdd3", "#e11d48", List.of("moda", "ropa", "chaqueta", "polera", "pantalon", "zapato", "vestido"), 4),
        new CategoriaBase("libros", "Libros", "libros", "book", "#fffbeb", "#fde68a", "#d97706", List.of("libro", "comic", "manga", "revista", "texto"), 5),
        new CategoriaBase("juguetes", "Juguetes", "juguetes", "puzzle-piece", "#f7fee7", "#d9f99d", "#65a30d", List.of("juguete", "figura", "muñeca", "peluche", "lego", "juego"), 6),
        new CategoriaBase("herramientas", "Herramientas", "herramientas", "wrench", "#f8fafc", "#e2e8f0", "#475569", List.of("herramienta", "taladro", "martillo", "sierra", "destornillador"), 7),
        new CategoriaBase("muebles", "Muebles", "muebles", "bed", "#fafaf9", "#e7e5e4", "#57534e", List.of("mueble", "silla", "mesa", "sofa", "cama", "estante"), 8),
        new CategoriaBase("infantil", "Infantil", "infantil", "child", "#fdf2f8", "#fbcfe8", "#db2777", List.of("infantil", "bebe", "niño", "niña", "cuna", "coche"), 9),
        new CategoriaBase("mascotas", "Mascotas", "mascotas", "paw", "#ecfdf5", "#a7f3d0", "#059669", List.of("mascota", "perro", "gato", "correa", "plato", "cama mascota"), 10)
    );

    @Autowired
    private WebClient publicacionWebClient;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoImagenRepository productoImagenRepository;

    @Autowired
    private ProductoCategoriaRepository productoCategoriaRepository;

    private ProductoDto convertirADto(Producto producto) {
        List<String> imagenes = productoImagenRepository.findByProductoIdOrderByOrden(producto.getProd_id())
            .stream()
            .map(ProductoImagen::getProd_img_url)
            .toList();

        return new ProductoDto(
            producto.getProd_id(),
            producto.getProd_nombre(),
            producto.getProd_est(),
            producto.getProd_categoria(),
            producto.getProd_precio(),
            producto.getPubl_id(),
            imagenes,
            producto.getProd_ubicacion_comuna(),
            producto.getProd_ubicacion_referencia(),
            producto.getProd_latitud_aprox(),
            producto.getProd_longitud_aprox()
        );
    }

    private ProductoCategoriaDto convertirCategoriaADto(ProductoCategoria categoria) {
        return new ProductoCategoriaDto(
            categoria.getCat_id(),
            categoria.getCat_nombre(),
            categoria.getCat_query(),
            categoria.getCat_icon(),
            categoria.getCat_bg_color(),
            categoria.getCat_border_color(),
            categoria.getCat_icon_color(),
            separarKeywords(categoria.getCat_keywords()),
            categoria.getCat_orden()
        );
    }

    @Transactional
    public ProductoDto crearProducto(CrearProducto nuevo) {
        if (nuevo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No es posible crear el producto");
        }

        List<String> imagenes = limpiarImagenes(nuevo.getProd_imagenes());
        String categoriaId = limpiarCategoria(nuevo.getProd_categoria());

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
        producto.setProd_categoria(categoriaId);
        producto.setProd_precio(nuevo.getProd_precio());
        producto.setPubl_id(nuevo.getPubl_id());
        producto.setProd_ubicacion_comuna(limpiarTexto(nuevo.getProd_ubicacion_comuna()));
        producto.setProd_ubicacion_referencia(limpiarTexto(nuevo.getProd_ubicacion_referencia()));
        producto.setProd_latitud_aprox(nuevo.getProd_latitud_aprox());
        producto.setProd_longitud_aprox(nuevo.getProd_longitud_aprox());

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

    @Transactional
    public List<ProductoCategoriaDto> obtenerCategorias() {
        asegurarCategoriasBase();
        return productoCategoriaRepository.findActivasOrdenadas()
            .stream()
            .map(this::convertirCategoriaADto)
            .toList();
    }

    public ProductoDto obtenerPorId(int idProducto) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        return convertirADto(producto);
    }

    @Transactional
    public ProductoDto modificarProducto(Integer idProducto, ActualizarProducto nuevo) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        } else {
            producto.setProd_nombre(nuevo.getProd_nombre());
            producto.setProd_est(nuevo.getProd_est());
            producto.setProd_categoria(limpiarCategoria(nuevo.getProd_categoria()));
            producto.setProd_precio(nuevo.getProd_precio());
            producto.setProd_ubicacion_comuna(limpiarTexto(nuevo.getProd_ubicacion_comuna()));
            producto.setProd_ubicacion_referencia(limpiarTexto(nuevo.getProd_ubicacion_referencia()));
            producto.setProd_latitud_aprox(nuevo.getProd_latitud_aprox());
            producto.setProd_longitud_aprox(nuevo.getProd_longitud_aprox());

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

    private void asegurarCategoriasBase() {
        for (CategoriaBase base : CATEGORIAS_BASE) {
            if (productoCategoriaRepository.existsById(base.id())) {
                continue;
            }

            ProductoCategoria categoria = new ProductoCategoria();
            categoria.setCat_id(base.id());
            categoria.setCat_nombre(base.nombre());
            categoria.setCat_query(base.query());
            categoria.setCat_icon(base.icon());
            categoria.setCat_bg_color(base.bgColor());
            categoria.setCat_border_color(base.borderColor());
            categoria.setCat_icon_color(base.iconColor());
            categoria.setCat_keywords(String.join(",", base.keywords()));
            categoria.setCat_orden(base.orden());
            categoria.setCat_activa(true);
            productoCategoriaRepository.save(categoria);
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

    private String limpiarCategoria(String valor) {
        String categoriaId = limpiarTexto(valor);
        if (categoriaId == null) {
            return null;
        }

        asegurarCategoriasBase();
        String normalizada = categoriaId.toLowerCase(Locale.ROOT);
        if (productoCategoriaRepository.findActivaPorId(normalizada).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria no disponible");
        }
        return normalizada;
    }

    private String limpiarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private List<String> separarKeywords(String valor) {
        if (valor == null || valor.isBlank()) {
            return List.of();
        }
        return Arrays.stream(valor.split(","))
            .map(String::trim)
            .filter(keyword -> !keyword.isBlank())
            .toList();
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

    private record CategoriaBase(
        String id,
        String nombre,
        String query,
        String icon,
        String bgColor,
        String borderColor,
        String iconColor,
        List<String> keywords,
        int orden
    ) {
    }
}

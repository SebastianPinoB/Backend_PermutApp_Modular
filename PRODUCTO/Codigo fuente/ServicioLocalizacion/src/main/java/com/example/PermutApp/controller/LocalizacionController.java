package com.example.PermutApp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import com.example.PermutApp.model.Request.DireccionRequest;
import com.example.PermutApp.model.Request.PuntoEncuentroRequest;
import com.example.PermutApp.model.Request.RegionRequest;
import com.example.PermutApp.model.Request.SugerenciaPuntoMedioRequest;
import com.example.PermutApp.model.Response.CiudadResponse;
import com.example.PermutApp.model.Response.ComunaResponse;
import com.example.PermutApp.model.Response.DireccionResponse;
import com.example.PermutApp.model.Response.EstacionMetroResponse;
import com.example.PermutApp.model.Response.PaisConRegionResponse;
import com.example.PermutApp.model.Response.PaisResponse;
import com.example.PermutApp.model.Response.PuntoEncuentroResponse;
import com.example.PermutApp.model.Response.RegionResponse;
import com.example.PermutApp.model.Response.SugerenciaPuntoMedioResponse;
import com.example.PermutApp.service.LocalizacionService;

@RestController
@RequestMapping("/localizacion")
@CrossOrigin(origins = "*") // Permite conectar frontend sin problemas de CORS
public class LocalizacionController {
    private final LocalizacionService localizacionService;

    public LocalizacionController(LocalizacionService localizacionService) {
        this.localizacionService = localizacionService;
    }

    // ==========================================
    // ENDPOINTS GEOGRAFICOS (CONSULTAS)
    // ==========================================

    @GetMapping("/paises")
    public ResponseEntity<List<PaisResponse>> obtenerPaises() {
        return ResponseEntity.ok(localizacionService.listarPaises());
    }

    @GetMapping("/paises-con-regiones")
    public ResponseEntity<List<PaisConRegionResponse>> obtenerPaisesConRegiones() {
        return ResponseEntity.ok(localizacionService.listarPaisesConRegiones());
    }

    @GetMapping("/regiones/pais/{paisId}")
    public ResponseEntity<List<RegionResponse>> obtenerRegionesPorPais(@PathVariable Integer paisId) {
        return ResponseEntity.ok(localizacionService.listarRegionesPorPais(paisId));
    }

    @GetMapping("/ciudades/region/{regionId}")
    public ResponseEntity<List<CiudadResponse>> obtenerCiudadesPorRegion(@PathVariable Integer regionId) {
        return ResponseEntity.ok(localizacionService.listarCiudadesPorRegion(regionId));
    }

    @GetMapping("/comunas/ciudad/{ciudadId}")
    public ResponseEntity<List<ComunaResponse>> obtenerComunasPorCiudad(@PathVariable Integer ciudadId) {
        return ResponseEntity.ok(localizacionService.listarComunasPorCiudad(ciudadId));
    }

    // ==========================================
    // ENDPOINTS DE DIRECCIONES (CRUD)
    // ==========================================

    @PostMapping("/direcciones")
    public ResponseEntity<DireccionResponse> crearDireccion(@RequestBody DireccionRequest request) {
        DireccionResponse nuevaDireccion = localizacionService.guardarDireccion(request);
        return new ResponseEntity<>(nuevaDireccion, HttpStatus.CREATED);
    }

    @GetMapping("/direcciones/{id}")
    public ResponseEntity<DireccionResponse> obtenerDireccionPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(localizacionService.buscarDireccionPorId(id));
    }

    // ==========================================
    // ENDPOINTS DE PUNTOS DE ENCUENTRO
    // ==========================================

    @PostMapping("/puntos-encuentro")
    public ResponseEntity<PuntoEncuentroResponse> crearPuntoEncuentro(@RequestBody PuntoEncuentroRequest request) {
        PuntoEncuentroResponse nuevoPunto = localizacionService.guardarPuntoEncuentro(request);
        return new ResponseEntity<>(nuevoPunto, HttpStatus.CREATED);
    }

    @GetMapping("/puntos-encuentro/comuna/{comunaId}")
    public ResponseEntity<List<PuntoEncuentroResponse>> obtenerPuntosPorComuna(@PathVariable Integer comunaId) {
        return ResponseEntity.ok(localizacionService.listarPuntosEncuentroPorComuna(comunaId));
    }

    // ==========================================
    // ENDPOINTS METRO
    // ==========================================

    @GetMapping("/metro/estaciones")
    public ResponseEntity<List<EstacionMetroResponse>> obtenerEstacionesMetro() {
        return ResponseEntity.ok(localizacionService.listarEstacionesMetro());
    }

    @GetMapping("/metro/lineas/{linea}")
    public ResponseEntity<List<EstacionMetroResponse>> obtenerEstacionesPorLinea(@PathVariable String linea) {
        return ResponseEntity.ok(localizacionService.listarEstacionesPorLinea(linea));
    }

    @PostMapping("/metro/punto-medio")
    public ResponseEntity<SugerenciaPuntoMedioResponse> sugerirMetroPuntoMedio(
            @Valid @RequestBody SugerenciaPuntoMedioRequest request) {
        return ResponseEntity.ok(localizacionService.sugerirMetroPuntoMedio(request));
    }

    // ==========================================
    // CREAR LA CADENA GEOGRAFICA
    // ==========================================

    @PostMapping("/paises")
    public ResponseEntity<PaisResponse> crearPais(@RequestBody PaisResponse request) {
        return new ResponseEntity<>(localizacionService.guardarPais(request), HttpStatus.CREATED);
    }

    @PostMapping("/regiones")
    public ResponseEntity<RegionResponse> crearRegion(@RequestBody RegionRequest request) {
        return new ResponseEntity<>(localizacionService.guardarRegion(request), HttpStatus.CREATED);
    }

    @PostMapping("/ciudades")
    public ResponseEntity<CiudadResponse> crearCiudad(@RequestBody CiudadResponse request) {
        return new ResponseEntity<>(localizacionService.guardarCiudad(request), HttpStatus.CREATED);
    }

    @PostMapping("/comunas")
    public ResponseEntity<ComunaResponse> crearComuna(@RequestBody ComunaResponse request) {
        return new ResponseEntity<>(localizacionService.guardarComuna(request), HttpStatus.CREATED);
    }

    // ==========================================
    // 📝 ENDPOINTS DE EDICIÓN (PUT)
    // ==========================================

    @PutMapping("/paises/{id}")
    public ResponseEntity<PaisResponse> actualizarPais(@PathVariable Integer id, @RequestBody PaisResponse request) {
        return ResponseEntity.ok(localizacionService.actualizarPais(id, request));
    }

    @PutMapping("/regiones/{id}")
    public ResponseEntity<RegionResponse> actualizarRegion(@PathVariable Integer id,
            @RequestBody RegionRequest request) {
        return ResponseEntity.ok(localizacionService.actualizarRegion(id, request));
    }

    @PutMapping("/ciudades/{id}")
    public ResponseEntity<CiudadResponse> actualizarCiudad(@PathVariable Integer id,
            @RequestBody CiudadResponse request) {
        return ResponseEntity.ok(localizacionService.actualizarCiudad(id, request));
    }

    @PutMapping("/comunas/{id}")
    public ResponseEntity<ComunaResponse> actualizarComuna(@PathVariable Integer id,
            @RequestBody ComunaResponse request) {
        return ResponseEntity.ok(localizacionService.actualizarComuna(id, request));
    }

    @PutMapping("/direcciones/{id}")
    public ResponseEntity<DireccionResponse> actualizarDireccion(@PathVariable Integer id,
            @RequestBody DireccionRequest request) {
        return ResponseEntity.ok(localizacionService.actualizarDireccion(id, request));
    }

    @PutMapping("/puntos-encuentro/{id}")
    public ResponseEntity<PuntoEncuentroResponse> actualizarPuntoEncuentro(@PathVariable Integer id,
            @RequestBody PuntoEncuentroRequest request) {
        return ResponseEntity.ok(localizacionService.actualizarPuntoEncuentro(id, request));
    }

    @PutMapping("/metro/estaciones/{id}")
    public ResponseEntity<EstacionMetroResponse> actualizarEstacionMetro(@PathVariable Integer id,
            @RequestBody EstacionMetroResponse request) {
        return ResponseEntity.ok(localizacionService.actualizarEstacionMetro(id, request));
    }

    // ==========================================
    // ENDPOINTS DE ELIMINACIÓN (DELETE)
    // ==========================================

    @DeleteMapping("/paises/{id}")
    public ResponseEntity<Void> eliminarPais(@PathVariable Integer id) {
        localizacionService.eliminarPais(id);
        return ResponseEntity.noContent().build(); // Retorna un estado 204 No Content
    }

    @DeleteMapping("/regiones/{id}")
    public ResponseEntity<Void> eliminarRegion(@PathVariable Integer id) {
        localizacionService.eliminarRegion(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/ciudades/{id}")
    public ResponseEntity<Void> eliminarCiudad(@PathVariable Integer id) {
        localizacionService.eliminarCiudad(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comunas/{id}")
    public ResponseEntity<Void> eliminarComuna(@PathVariable Integer id) {
        localizacionService.eliminarComuna(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/direcciones/{id}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable Integer id) {
        localizacionService.eliminarDireccion(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/puntos-encuentro/{id}")
    public ResponseEntity<Void> eliminarPuntoEncuentro(@PathVariable Integer id) {
        localizacionService.eliminarPuntoEncuentro(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/metro/estaciones/{id}")
    public ResponseEntity<Void> eliminarEstacionMetro(@PathVariable Integer id) {
        localizacionService.eliminarEstacionMetro(id);
        return ResponseEntity.noContent().build();
    }
}

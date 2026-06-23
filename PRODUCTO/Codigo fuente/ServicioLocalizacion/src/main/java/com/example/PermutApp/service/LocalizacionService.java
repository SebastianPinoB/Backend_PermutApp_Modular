package com.example.PermutApp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PermutApp.exception.ElementoDuplicadoException;
import com.example.PermutApp.model.Ciudad;
import com.example.PermutApp.model.Comuna;
import com.example.PermutApp.model.Direccion;
import com.example.PermutApp.model.EstacionMetro;
import com.example.PermutApp.model.Pais;
import com.example.PermutApp.model.PuntoEncuentro;
import com.example.PermutApp.model.Region;
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
import com.example.PermutApp.repository.CiudadRepository;
import com.example.PermutApp.repository.ComunaRepository;
import com.example.PermutApp.repository.DireccionRepository;
import com.example.PermutApp.repository.EstacionMetroRepository;
import com.example.PermutApp.repository.PaisRepository;
import com.example.PermutApp.repository.PuntoEncuentroRepository;
import com.example.PermutApp.repository.RegionRepository;

@Service
public class LocalizacionService {
   private final PaisRepository paisRepository;
   private final RegionRepository regionRepository;
   private final CiudadRepository ciudadRepository;
   private final ComunaRepository comunaRepository;
   private final DireccionRepository direccionRepository;
   private final PuntoEncuentroRepository puntoEncuentroRepository;
   private final EstacionMetroRepository estacionMetroRepository;

   // Inyección por constructor
   public LocalizacionService(PaisRepository paisRepository, RegionRepository regionRepository,
         CiudadRepository ciudadRepository, ComunaRepository comunaRepository,
         DireccionRepository direccionRepository, PuntoEncuentroRepository puntoEncuentroRepository,
         EstacionMetroRepository estacionMetroRepository) {
      this.paisRepository = paisRepository;
      this.regionRepository = regionRepository;
      this.ciudadRepository = ciudadRepository;
      this.comunaRepository = comunaRepository;
      this.direccionRepository = direccionRepository;
      this.puntoEncuentroRepository = puntoEncuentroRepository;
      this.estacionMetroRepository = estacionMetroRepository;
   }

   // --- MÉTODOS GEOGRÁFICOS ---

   public List<PaisResponse> listarPaises() {
      return paisRepository.findAll().stream()
            .map(pais -> {
               PaisResponse res = new PaisResponse();
               res.setId(pais.getPaisId());
               res.setNombre(pais.getPaisNombre());
               return res;
            })
            .collect(Collectors.toList());
   }

   public List<PaisConRegionResponse> listarPaisesConRegiones() {
      return paisRepository.findAll().stream()
            .map(pais -> {
               PaisConRegionResponse res = new PaisConRegionResponse();

               res.setId(pais.getPaisId());
               res.setNombre(pais.getPaisNombre());

               // Mapeamos su lista interna de regiones a RegionResponse
               List<RegionResponse> regionesDto = pais.getRegiones().stream()
                     .map(this::mapearARegionResponse)
                     .collect(Collectors.toList());

               res.setRegiones(regionesDto);
               return res;
            })
            .collect(Collectors.toList());
   }

   public List<RegionResponse> listarRegionesPorPais(Integer paisId) {
      return regionRepository.findByPaisPaisId(paisId).stream()
            .map(this::mapearARegionResponse)
            .collect(Collectors.toList());
   }

   public List<CiudadResponse> listarCiudadesPorRegion(Integer regionId) {
      return ciudadRepository.findByRegionRegiId(regionId).stream()
            .map(ciudad -> {
               CiudadResponse res = new CiudadResponse();
               res.setId(ciudad.getCiudadId());
               res.setNombre(ciudad.getCiudadNombre());
               res.setRegionId(ciudad.getRegion().getRegiId());
               return res;
            })
            .collect(Collectors.toList());
   }

   public List<ComunaResponse> listarComunasPorCiudad(Integer ciudadId) {
      return comunaRepository.findByCiudadCiudadId(ciudadId).stream()
            .map(comuna -> {
               ComunaResponse res = new ComunaResponse();
               res.setId(comuna.getComuId());
               res.setNombre(comuna.getComuNombre());
               res.setCiudadId(comuna.getCiudad().getCiudadId());
               return res;
            })
            .collect(Collectors.toList());
   }

   // ===================================================
   // 🔄 MÉTODOS DE CREACIÓN INTELIGENTE (FIND OR CREATE)
   // ===================================================

   @Transactional
   public PaisResponse guardarPais(PaisResponse request) {
      Pais pais = paisRepository.findByPaisNombreIgnoreCase(request.getNombre())
            .orElseGet(() -> {
               Pais nuevoPais = new Pais();
               nuevoPais.setPaisNombre(request.getNombre());
               return paisRepository.save(nuevoPais);
            });

      PaisResponse res = new PaisResponse();
      res.setId(pais.getPaisId());
      res.setNombre(pais.getPaisNombre());
      return res;
   }

   @Transactional
   public RegionResponse guardarRegion(RegionRequest request) {
      Pais pais = paisRepository.findById(request.getPaisId())
            .orElseThrow(() -> new RuntimeException("País no encontrado con ID: " + request.getPaisId()));

      Region region = regionRepository.findByNombreYPais(request.getNombre(), request.getPaisId())
            .orElseGet(() -> {
               Region nuevaRegion = new Region();
               nuevaRegion.setRegiNombre(request.getNombre());
               nuevaRegion.setPais(pais);
               return regionRepository.save(nuevaRegion);
            });

      return mapearARegionResponse(region);
   }

   @Transactional
   public CiudadResponse guardarCiudad(CiudadResponse request) {
      Region region = regionRepository.findById(request.getRegionId())
            .orElseThrow(() -> new RuntimeException("Región no encontrada con ID: " + request.getRegionId()));

      Ciudad ciudad = ciudadRepository.findByNombreYRegion(request.getNombre(), request.getRegionId())
            .orElseGet(() -> {
               Ciudad nuevaCiudad = new Ciudad();
               nuevaCiudad.setCiudadNombre(request.getNombre());
               nuevaCiudad.setRegion(region);
               return ciudadRepository.save(nuevaCiudad);
            });

      CiudadResponse res = new CiudadResponse();
      res.setId(ciudad.getCiudadId());
      res.setNombre(ciudad.getCiudadNombre());
      res.setRegionId(ciudad.getRegion().getRegiId());
      return res;
   }

   @Transactional
   public ComunaResponse guardarComuna(ComunaResponse request) {
      Ciudad ciudad = ciudadRepository.findById(request.getCiudadId())
            .orElseThrow(() -> new RuntimeException("Ciudad no encontrada con ID: " + request.getCiudadId()));

      Comuna comuna = comunaRepository.findByNombreYCiudad(request.getNombre(), request.getCiudadId())
            .orElseGet(() -> {
               Comuna nuevaComuna = new Comuna();
               nuevaComuna.setComuNombre(request.getNombre());
               nuevaComuna.setCiudad(ciudad);
               return comunaRepository.save(nuevaComuna);
            });

      ComunaResponse res = new ComunaResponse();
      res.setId(comuna.getComuId());
      res.setNombre(comuna.getComuNombre());
      res.setCiudadId(comuna.getCiudad().getCiudadId());
      return res;
   }

   @Transactional
   public EstacionMetroResponse guardarEstacionMetro(EstacionMetroResponse request) {
      if (estacionMetroRepository.existsByNombreIgnoreCaseAndLineaIgnoreCase(request.getNombre(), request.getLinea())) {
         throw new ElementoDuplicadoException(
               "La estación '" + request.getNombre() + "' ya existe en la línea " + request.getLinea() + ".");
      }

      EstacionMetro metro = new EstacionMetro();
      metro.setNombre(request.getNombre());
      metro.setLinea(request.getLinea());
      metro.setOrden(request.getOrden());
      metro.setEsCombinacion(request.getEsCombinacion());
      metro.setLatitud(request.getLatitud());
      metro.setLongitud(request.getLongitud());
      metro.setDireccion(request.getDireccion());
      metro.setComuna(request.getComuna());

      EstacionMetro guardado = estacionMetroRepository.save(metro);
      return mapearAEstacionMetroResponse(guardado);
   }

   // --- MÉTODOS DE DIRECCIONES ---

   @Transactional
   public DireccionResponse guardarDireccion(DireccionRequest request) {
      Comuna comuna = comunaRepository.findById(request.getComunaId())
            .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + request.getComunaId()));

      Direccion direccion = new Direccion();
      direccion.setAddCalle(request.getNombreCalle());
      direccion.setAddNumero(request.getNumero()); // Mapea a tu int de la entidad
      direccion.setAddLetra(request.getAddLetra());
      direccion.setComuna(comuna);

      Direccion guardada = direccionRepository.save(direccion);
      return mapearADireccionResponse(guardada);
   }

   public DireccionResponse buscarDireccionPorId(Integer id) {
      Direccion direccion = direccionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dirección no encontrada con ID: " + id));
      return mapearADireccionResponse(direccion);
   }

   // --- MÉTODOS DE PUNTOS DE ENCUENTRO ---

   @Transactional
   public PuntoEncuentroResponse guardarPuntoEncuentro(PuntoEncuentroRequest request) {
      Comuna comuna = comunaRepository.findById(request.getComunaId())
            .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + request.getComunaId()));

      PuntoEncuentro punto = new PuntoEncuentro();
      punto.setLatitud(request.getLatitud());
      punto.setLongitud(request.getLongitud());
      punto.setReferencias(request.getReferencias());
      punto.setComuna(comuna);

      PuntoEncuentro guardado = puntoEncuentroRepository.save(punto);
      return mapearAPuntoEncuentroResponse(guardado);
   }

   public List<PuntoEncuentroResponse> listarPuntosEncuentroPorComuna(Integer comunaId) {
      return puntoEncuentroRepository.findByComunaComuId(comunaId).stream()
            .map(this::mapearAPuntoEncuentroResponse)
            .collect(Collectors.toList());
   }

   // --- MÉTODOS DE METRO ---

   public List<EstacionMetroResponse> listarEstacionesMetro() {
      return estacionMetroRepository.findAll().stream()
            .map(this::mapearAEstacionMetroResponse)
            .collect(Collectors.toList());
   }

   public List<EstacionMetroResponse> listarEstacionesPorLinea(String linea) {
      return estacionMetroRepository.findByLinea(linea).stream()
            .map(this::mapearAEstacionMetroResponse)
            .collect(Collectors.toList());
   }

   public SugerenciaPuntoMedioResponse sugerirMetroPuntoMedio(SugerenciaPuntoMedioRequest request) {
      double puntoMedioLatitud = (request.getLatitudOrigen() + request.getLatitudDestino()) / 2;
      double puntoMedioLongitud = (request.getLongitudOrigen() + request.getLongitudDestino()) / 2;

      EstacionMetro estacion = estacionMetroRepository.findByLatitudIsNotNullAndLongitudIsNotNull().stream()
            .min((actual, candidata) -> Double.compare(
                  calcularDistanciaKm(puntoMedioLatitud, puntoMedioLongitud, actual.getLatitud(), actual.getLongitud()),
                  calcularDistanciaKm(puntoMedioLatitud, puntoMedioLongitud, candidata.getLatitud(), candidata.getLongitud())))
            .orElseThrow(() -> new RuntimeException("No hay estaciones de Metro cargadas con coordenadas."));

      SugerenciaPuntoMedioResponse response = new SugerenciaPuntoMedioResponse();
      response.setPuntoMedioLatitud(redondearCoordenada(puntoMedioLatitud));
      response.setPuntoMedioLongitud(redondearCoordenada(puntoMedioLongitud));
      response.setEstacionSugerida(mapearAEstacionMetroResponse(estacion));
      response.setDistanciaPuntoMedioKm(redondearDistancia(calcularDistanciaKm(
            puntoMedioLatitud, puntoMedioLongitud, estacion.getLatitud(), estacion.getLongitud())));
      response.setDistanciaOrigenKm(redondearDistancia(calcularDistanciaKm(
            request.getLatitudOrigen(), request.getLongitudOrigen(), estacion.getLatitud(), estacion.getLongitud())));
      response.setDistanciaDestinoKm(redondearDistancia(calcularDistanciaKm(
            request.getLatitudDestino(), request.getLongitudDestino(), estacion.getLatitud(), estacion.getLongitud())));
      response.setCriterio("Estacion de Metro de Santiago mas cercana al punto medio geografico entre ambos usuarios.");
      return response;
   }

   // ==========================================
   // 📝 MÉTODOS DE EDICIÓN (UPDATE)
   // ==========================================

   @Transactional
   public PaisResponse actualizarPais(Integer id, PaisResponse request) {
      Pais pais = paisRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("País no encontrado con ID: " + id));
      pais.setPaisNombre(request.getNombre());
      Pais guardado = paisRepository.save(pais);

      PaisResponse res = new PaisResponse();
      res.setId(guardado.getPaisId());
      res.setNombre(guardado.getPaisNombre());
      return res;
   }

   @Transactional
   public RegionResponse actualizarRegion(Integer id, RegionRequest request) {
      Region region = regionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Región no encontrada con ID: " + id));
      Pais pais = paisRepository.findById(request.getPaisId())
            .orElseThrow(() -> new RuntimeException("País no encontrado con ID: " + request.getPaisId()));

      region.setRegiNombre(request.getNombre());
      region.setPais(pais);
      Region guardada = regionRepository.save(region);

      return mapearARegionResponse(guardada);
   }

   @Transactional
   public CiudadResponse actualizarCiudad(Integer id, CiudadResponse request) {
      Ciudad ciudad = ciudadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ciudad no encontrada con ID: " + id));
      Region region = regionRepository.findById(request.getRegionId())
            .orElseThrow(() -> new RuntimeException("Región no encontrada con ID: " + request.getRegionId()));

      ciudad.setCiudadNombre(request.getNombre());
      ciudad.setRegion(region);
      Ciudad guardada = ciudadRepository.save(ciudad);

      CiudadResponse res = new CiudadResponse();
      res.setId(guardada.getCiudadId());
      res.setNombre(guardada.getCiudadNombre());
      res.setRegionId(guardada.getRegion().getRegiId());
      return res;
   }

   @Transactional
   public ComunaResponse actualizarComuna(Integer id, ComunaResponse request) {
      Comuna comuna = comunaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + id));
      Ciudad ciudad = ciudadRepository.findById(request.getCiudadId())
            .orElseThrow(() -> new RuntimeException("Ciudad no encontrada con ID: " + request.getCiudadId()));

      comuna.setComuNombre(request.getNombre());
      comuna.setCiudad(ciudad);
      Comuna guardada = comunaRepository.save(comuna);

      ComunaResponse res = new ComunaResponse();
      res.setId(guardada.getComuId());
      res.setNombre(guardada.getComuNombre());
      res.setCiudadId(guardada.getCiudad().getCiudadId());
      return res;
   }

   @Transactional
   public DireccionResponse actualizarDireccion(Integer id, DireccionRequest request) {
      Direccion direccion = direccionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dirección no encontrada con ID: " + id));
      Comuna comuna = comunaRepository.findById(request.getComunaId())
            .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + request.getComunaId()));

      direccion.setAddCalle(request.getNombreCalle());
      direccion.setAddNumero(request.getNumero());
      direccion.setAddLetra(request.getAddLetra());
      direccion.setComuna(comuna);
      Direccion guardada = direccionRepository.save(direccion);

      return mapearADireccionResponse(guardada);
   }

   @Transactional
   public PuntoEncuentroResponse actualizarPuntoEncuentro(Integer id, PuntoEncuentroRequest request) {
      PuntoEncuentro punto = puntoEncuentroRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Punto de encuentro no encontrado con ID: " + id));
      Comuna comuna = comunaRepository.findById(request.getComunaId())
            .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + request.getComunaId()));

      punto.setLatitud(request.getLatitud());
      punto.setLongitud(request.getLongitud());
      punto.setReferencias(request.getReferencias());
      punto.setComuna(comuna);
      PuntoEncuentro guardado = puntoEncuentroRepository.save(punto);

      return mapearAPuntoEncuentroResponse(guardado);
   }

   @Transactional
   public EstacionMetroResponse actualizarEstacionMetro(Integer id, EstacionMetroResponse request) {
      EstacionMetro metro = estacionMetroRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Estación de metro no encontrada con ID: " + id));

      metro.setNombre(request.getNombre());
      metro.setLinea(request.getLinea());
      metro.setOrden(request.getOrden());
      metro.setEsCombinacion(request.getEsCombinacion());
      metro.setLatitud(request.getLatitud());
      metro.setLongitud(request.getLongitud());
      metro.setDireccion(request.getDireccion());
      metro.setComuna(request.getComuna());
      EstacionMetro guardado = estacionMetroRepository.save(metro);

      return mapearAEstacionMetroResponse(guardado);
   }

   // ==========================================
   // 🗑️ MÉTODOS DE ELIMINACIÓN (DELETE)
   // ==========================================

   @Transactional
   public void eliminarPais(Integer id) {
      if (!paisRepository.existsById(id))
         throw new RuntimeException("País no encontrado");
      paisRepository.deleteById(id);
   }

   @Transactional
   public void eliminarRegion(Integer id) {
      if (!regionRepository.existsById(id))
         throw new RuntimeException("Región no encontrada");
      regionRepository.deleteById(id);
   }

   @Transactional
   public void eliminarCiudad(Integer id) {
      if (!ciudadRepository.existsById(id))
         throw new RuntimeException("Ciudad no encontrada");
      ciudadRepository.deleteById(id);
   }

   @Transactional
   public void eliminarComuna(Integer id) {
      if (!comunaRepository.existsById(id))
         throw new RuntimeException("Comuna no encontrada");
      comunaRepository.deleteById(id);
   }

   @Transactional
   public void eliminarDireccion(Integer id) {
      if (!direccionRepository.existsById(id))
         throw new RuntimeException("Dirección no encontrada");
      direccionRepository.deleteById(id);
   }

   @Transactional
   public void eliminarPuntoEncuentro(Integer id) {
      if (!puntoEncuentroRepository.existsById(id))
         throw new RuntimeException("Punto de encuentro no encontrado");
      puntoEncuentroRepository.deleteById(id);
   }

   @Transactional
   public void eliminarEstacionMetro(Integer id) {
      if (!estacionMetroRepository.existsById(id))
         throw new RuntimeException("Estación de metro no encontrada");
      estacionMetroRepository.deleteById(id);
   }

   // --- REUSABLE HELPERS (MAPEOS INTERNOS) ---

   private RegionResponse mapearARegionResponse(Region region) {
      RegionResponse res = new RegionResponse();
      res.setId(region.getRegiId());
      res.setNombre(region.getRegiNombre());
      res.setPaisId(region.getPais().getPaisId());
      return res;
   }

   private DireccionResponse mapearADireccionResponse(Direccion direccion) {
      DireccionResponse res = new DireccionResponse();
      res.setId(direccion.getAddId());
      res.setNombreCalle(direccion.getAddCalle());
      res.setAddLetra(direccion.getAddLetra());
      res.setNumero(direccion.getAddNumero());
      res.setComunaId(direccion.getComuna().getComuId());
      res.setNombreComuna(direccion.getComuna().getComuNombre());
      return res;
   }

   private PuntoEncuentroResponse mapearAPuntoEncuentroResponse(PuntoEncuentro punto) {
      PuntoEncuentroResponse res = new PuntoEncuentroResponse();
      res.setId(punto.getSugIdPuntoEncuentro());
      res.setLatitud(punto.getLatitud());
      res.setLongitud(punto.getLongitud());
      res.setReferencias(punto.getReferencias());
      res.setComunaId(punto.getComuna().getComuId());
      res.setNombreComuna(punto.getComuna().getComuNombre());
      return res;
   }

   private EstacionMetroResponse mapearAEstacionMetroResponse(EstacionMetro metro) {
      EstacionMetroResponse res = new EstacionMetroResponse();
      res.setId(metro.getId());
      res.setNombre(metro.getNombre());
      res.setLinea(metro.getLinea());
      res.setOrden(metro.getOrden());
      res.setEsCombinacion(metro.getEsCombinacion());
      res.setLatitud(metro.getLatitud());
      res.setLongitud(metro.getLongitud());
      res.setDireccion(metro.getDireccion());
      res.setComuna(metro.getComuna());
      return res;
   }

   private double calcularDistanciaKm(double latitudA, double longitudA, double latitudB, double longitudB) {
      final double radioTierraKm = 6371.0;
      double deltaLatitud = Math.toRadians(latitudB - latitudA);
      double deltaLongitud = Math.toRadians(longitudB - longitudA);
      double a = Math.sin(deltaLatitud / 2) * Math.sin(deltaLatitud / 2)
            + Math.cos(Math.toRadians(latitudA)) * Math.cos(Math.toRadians(latitudB))
                  * Math.sin(deltaLongitud / 2) * Math.sin(deltaLongitud / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return radioTierraKm * c;
   }

   private double redondearDistancia(double valor) {
      return Math.round(valor * 100.0) / 100.0;
   }

   private double redondearCoordenada(double valor) {
      return Math.round(valor * 1_000_000.0) / 1_000_000.0;
   }
}

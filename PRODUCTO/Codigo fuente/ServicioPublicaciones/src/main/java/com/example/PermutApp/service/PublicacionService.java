package com.example.PermutApp.service;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.dto.UsuarioDto;
import com.example.PermutApp.model.entities.Publicacion;
import com.example.PermutApp.model.request.ActualizarPublicacion;
import com.example.PermutApp.model.request.CrearPublicacion;
import com.example.PermutApp.repository.PublicacionRepository;

@Service
public class PublicacionService {
   private final WebClient usuarioWebClient;
   private final PublicacionRepository publicacionRepository;

   public PublicacionService(WebClient usuarioWebClient, PublicacionRepository publicacionRepository) {
      this.usuarioWebClient = usuarioWebClient;
      this.publicacionRepository = publicacionRepository;
   }

   public Publicacion crearPublicacion(CrearPublicacion nueva, String authorization) {
      UsuarioDto usuario;
      try {
         var request = usuarioWebClient.get().uri("/usuario/{idUsuario}", nueva.getUsu_id());
         if (authorization != null && !authorization.isBlank()) {
            request = request.header("Authorization", authorization);
         }
         usuario = request.retrieve().bodyToMono(UsuarioDto.class).block();
      } catch (Exception e) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }
      if (usuario == null) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }

      Publicacion publicacion = new Publicacion();
      publicacion.setPubl_titulo(nueva.getPubl_titulo());
      publicacion.setPubl_descripcion(nueva.getPubl_descripcion());
      publicacion.setPubl_fech_creacion(new Date());
      publicacion.setPubl_activo(true);
      publicacion.setPubl_autor_id(nueva.getUsu_id());
      return publicacionRepository.save(publicacion);
   }

   public List<Publicacion> obtenerTodos() {
      return publicacionRepository.listarActivas();
   }

   public Publicacion obtenerPorId(int idPublicacion) {
      return publicacionRepository.findById(idPublicacion)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada"));
   }

   public Publicacion actualizarPublicacion(Integer idPublicacion, ActualizarPublicacion nueva) {
      Publicacion publicacion = obtenerPorId(idPublicacion);
      publicacion.setPubl_titulo(nueva.getPubl_titulo());
      publicacion.setPubl_descripcion(nueva.getPubl_descripcion());
      return publicacionRepository.save(publicacion);
   }

   public String eliminarPublicacion(int idPublicacion) {
      Publicacion publicacion = obtenerPorId(idPublicacion);
      publicacion.setPubl_activo(false);
      publicacionRepository.save(publicacion);
      return "Publicacion desactivada correctamente";
   }

   @Transactional
   public void finalizarPermuta(int publicacionAutorId, int publicacionOfrecidaId) {
      if (publicacionAutorId == publicacionOfrecidaId) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las publicaciones deben ser distintas");
      }
      Publicacion original = obtenerPorId(publicacionAutorId);
      Publicacion ofrecida = obtenerPorId(publicacionOfrecidaId);
      original.setPubl_activo(false);
      ofrecida.setPubl_activo(false);
      publicacionRepository.saveAll(List.of(original, ofrecida));
   }
}

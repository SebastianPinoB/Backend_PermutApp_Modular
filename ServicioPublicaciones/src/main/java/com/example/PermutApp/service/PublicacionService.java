package com.example.PermutApp.service;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.dto.UsuarioDto;
import com.example.PermutApp.model.entities.Publicacion;
import com.example.PermutApp.model.request.ActualizarPublicacion;
import com.example.PermutApp.model.request.CrearPublicacion;
import com.example.PermutApp.repository.PublicacionRepository;


@Service
public class PublicacionService {

   @Autowired
   private WebClient usuarioWebClient;

   @Autowired
   public PublicacionRepository publicacionRepository;

   public Publicacion crearPublicacion(CrearPublicacion nueva){
      UsuarioDto usuario = null;

      try {
         usuario = usuarioWebClient.get()
         .uri("/usuario/{idUsuario}", nueva.getUsu_id())
         .retrieve()
         .bodyToMono(UsuarioDto.class)
         .block();
      } catch (Exception e) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ENDPOINT ---> Usuario no encontrado");
      }

      if(usuario == null){
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
   public List<Publicacion> obtenerTodos(){
      return publicacionRepository.findAll();
   }
/*
   
   public Publicacion obtenerPorId(int idPublicacion){
      Publicacion publicacion = publicacionRepository.findById(idPublicacion).orElse(null);
      if(publicacion == null){
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrado");
      }
      return publicacion;
   }

   public Publicacion crearPublicacion(CrearPublicacion nueva){
      //Falta asignarle un creador
      Publicacion publicacion = new Publicacion();
      publicacion.setPubl_titulo(nueva.getPubl_titulo());
      publicacion.setPubl_descripcion(nueva.getPubl_descripcion());
      publicacion.setPubl_fech_creacion(new Date());
      publicacion.setPubl_activo(true);
      return publicacionRepository.save(publicacion);
   }

   public Publicacion actualizarPublicacion(Integer idPublicacion, ActualizarPublicacion nueva){
      Publicacion publicacion = publicacionRepository.findById(idPublicacion).orElse(null);
      if(publicacion == null){
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada");
      }else{
         publicacion.setPubl_titulo(nueva.getPubl_titulo());
         publicacion.setPubl_descripcion(nueva.getPubl_descripcion());

         return publicacionRepository.save(publicacion);
      }
   }

   public String eliminarPublicacion(int idPublicacion){
      if(publicacionRepository.existsById(idPublicacion)){
         publicacionRepository.deleteById(idPublicacion);
         return "Publicacion eliminada correctamente";
      }else{
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada");
      }

   }

   public Publicacion cambiarEstadoPublicacion(int idPublicacion){
      Publicacion publicacion = publicacionRepository.findById(idPublicacion).orElse(null);
      if(publicacion == null){
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicacion no encontrada");
      }else{
         if(publicacion.getPubl_activo()==true) {
            publicacion.setPubl_activo(false);
         }else{
            publicacion.setPubl_activo(true);
         }
         return publicacionRepository.save(publicacion);
      }
   }
*/
}

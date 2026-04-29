package com.example.PermutApp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PermutApp.model.entities.Publicacion;
import com.example.PermutApp.model.request.ActualizarPublicacion;
import com.example.PermutApp.model.request.CrearPublicacion;
import com.example.PermutApp.service.PublicacionService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("publicacion")
public class PublicacionController {
   @Autowired
   private PublicacionService publicacionService;

   @PostMapping("")
   public Publicacion crearPublicacion(@RequestBody CrearPublicacion nueva) {
      return publicacionService.crearPublicacion(nueva);
   }
   
   @GetMapping("")
   public List<Publicacion> obtenerTodasPublicaciones() {
      return publicacionService.obtenerTodos();
   }
/* 
   

   @GetMapping("/{idPublicacion}")
   public Publicacion buscarPorIdPublicacion(@PathVariable Integer idPublicacion) {
      return publicacionService.obtenerPorId(idPublicacion);
   }

   @PostMapping("")
   public Publicacion crearPublicacion(@RequestBody CrearPublicacion nueva) {
      return publicacionService.crearPublicacion(nueva);
   }
   
   @PutMapping("/{idPublicacion}")
   public Publicacion modificarPublicacion(@PathVariable Integer idPublicacion, @RequestBody ActualizarPublicacion nueva) {
      return publicacionService.actualizarPublicacion(idPublicacion, nueva);
   }


   //Esto deberia ir dentro de la modificacion de la publicacion
   /*@PutMapping("/{idPublicacion}")
   public Publicacion cambiarEstadoPublicacion(@PathVariable Integer idPublicacion) {
      return publicacionService.cambiarEstadoPublicacion(idPublicacion);
   }
   
   @DeleteMapping("/{idPublicacion}")
   public String eliminarPublicacion(@PathVariable Integer idPublicacion){
      return publicacionService.eliminarPublicacion(idPublicacion);
   }
*/
}

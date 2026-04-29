package com.example.PermutApp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.request.ActualizarUsuario;
import com.example.PermutApp.model.request.CrearUsuario;
import com.example.PermutApp.service.UsuarioService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequestMapping("usuario")
@RestController
public class UsuarioController {
   @Autowired
   private UsuarioService usuarioService;

   @GetMapping("")
   public List<Usuario> obtenerTodosUsuarios() {
       return usuarioService.obtenerTodos();
   }

   @GetMapping("/{idUsuario}")
   public Usuario buscarPorIdUsuario(@PathVariable Integer idUsuario) {
      return usuarioService.obtenerPorId(idUsuario);
   }
   
   @PostMapping("")
   public Usuario agregarUsuario(@RequestBody CrearUsuario usuario) {
      return usuarioService.crearUsuario(usuario);
   }

   @PutMapping("/{id}")
   public Usuario actualizUsuario(@PathVariable Integer id ,@RequestBody ActualizarUsuario nuevo) {
      return usuarioService.actualizarUsuario(id, nuevo);
   }
   
   @DeleteMapping("/{id}")
   public String eliminarUsuario(@PathVariable Integer id){
      return usuarioService.eliminarUsuario(id);
   }

}

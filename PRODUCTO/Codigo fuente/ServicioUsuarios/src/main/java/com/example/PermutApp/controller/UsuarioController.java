package com.example.PermutApp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.dto.UsuarioDto;
import com.example.PermutApp.model.request.ActualizarUsuario;
import com.example.PermutApp.model.request.CrearUsuario;
import com.example.PermutApp.service.UsuarioService;

import jakarta.validation.Valid;

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
   public List<UsuarioDto> obtenerTodosUsuarios() {
       return usuarioService.obtenerTodos();
   }

   @GetMapping("/{idUsuario}")
   public UsuarioDto buscarPorIdUsuario(@PathVariable Integer idUsuario) {
      return usuarioService.obtenerPorId(idUsuario);
   }
   
   @PostMapping("")
   public UsuarioDto agregarUsuario(@Valid @RequestBody CrearUsuario usuario) {
      return usuarioService.crearUsuario(usuario);
   }

   @PutMapping("/{id}")
   public UsuarioDto actualizUsuario(@PathVariable Integer id, @Valid @RequestBody ActualizarUsuario nuevo) {
      return usuarioService.actualizarUsuario(id, nuevo);
   }
   
   @DeleteMapping("/{id}")
   public String eliminarUsuario(@PathVariable Integer id, Authentication authentication){
      UsuarioDto usuario = usuarioService.obtenerPorId(id);
      if (authentication == null || !usuario.usu_email().equalsIgnoreCase(authentication.getName())) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes eliminar tu propia cuenta");
      }
      return usuarioService.eliminarUsuario(id);
   }

}

package com.example.PermutApp.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.request.ActualizarUsuario;
import com.example.PermutApp.model.request.CrearUsuario;
import com.example.PermutApp.repository.UsuarioRepository;

@Service
public class UsuarioService {
   
   @Autowired
   private UsuarioRepository usuarioRepository;

   public List<Usuario> obtenerTodos(){
      return usuarioRepository.findAll();
   }
   
   public Usuario obtenerPorId(int idUsuario){
      Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
      if(usuario == null){
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      } 
      return usuario;
   }

   public Usuario crearUsuario(CrearUsuario nuevo){
      Usuario nuevoUsuario = new Usuario();
      nuevoUsuario.setUsu_numrun(nuevo.getUsu_numrun());
      nuevoUsuario.setUsu_dvrun(nuevo.getUsu_dvrun());
      nuevoUsuario.setUsu_pri_nombre(nuevo.getUsu_pri_nombre());
      nuevoUsuario.setUsu_seg_nombre(nuevo.getUsu_seg_nombre());
      nuevoUsuario.setUsu_pri_apellido(nuevo.getUsu_pri_apellido());
      nuevoUsuario.setUsu_seg_apellido(nuevo.getUsu_seg_apellido());
      nuevoUsuario.setUsu_email(nuevo.getUsu_email());
      nuevoUsuario.setUsu_pass(nuevo.getUsu_pass());
      nuevoUsuario.setUsu_prom_rep(nuevo.getUsu_prom_rep());
      nuevoUsuario.setUsu_activo(true);

      return usuarioRepository.save(nuevoUsuario);
   }

   public String eliminarUsuario(int idUsuario){
      if(usuarioRepository.existsById(idUsuario)){
         usuarioRepository.deleteById(idUsuario);
         return "Usuario eliminado correctamente";
      }else{
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }
   }

   public Usuario actualizarUsuario(Integer idUsuario, ActualizarUsuario nuevo){
      Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
      if(usuario == null){
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario con ID " + idUsuario + " no encontrado");
      }else{
         // Si la contrasenia queda vacia, se guardará de esa manera, entonces hay que validarlo
         usuario.setUsu_numrun(nuevo.getUsu_numrun());
         usuario.setUsu_dvrun(nuevo.getUsu_dvrun());
         usuario.setUsu_pri_nombre(nuevo.getUsu_pri_nombre());
         usuario.setUsu_seg_nombre(nuevo.getUsu_seg_nombre());
         usuario.setUsu_pri_apellido(nuevo.getUsu_pri_apellido());
         usuario.setUsu_seg_apellido(nuevo.getUsu_seg_apellido());
         usuario.setUsu_email(nuevo.getUsu_email());
         usuario.setUsu_pass(nuevo.getUsu_pass());
         usuario.setUsu_prom_rep(nuevo.getUsu_prom_rep());
         return usuarioRepository.save(usuario);
      }
   }

}

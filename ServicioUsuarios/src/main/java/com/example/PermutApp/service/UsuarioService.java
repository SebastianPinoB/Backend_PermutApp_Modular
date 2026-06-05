package com.example.PermutApp.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.Entities.VerificacionIdentidad.EstadoVerificacion;
import com.example.PermutApp.model.dto.UsuarioDto;
import com.example.PermutApp.model.request.ActualizarUsuario;
import com.example.PermutApp.model.request.CrearUsuario;
import com.example.PermutApp.repository.UsuarioRepository;
import com.example.PermutApp.repository.VerificacionIdentidadRepository;

@Service
public class UsuarioService {

   private final UsuarioRepository usuarioRepository;
   private final VerificacionIdentidadRepository verificacionIdentidadRepository;
   private final PasswordEncoder passwordEncoder;

   public UsuarioService(
         UsuarioRepository usuarioRepository,
         VerificacionIdentidadRepository verificacionIdentidadRepository,
         PasswordEncoder passwordEncoder) {
      this.usuarioRepository = usuarioRepository;
      this.verificacionIdentidadRepository = verificacionIdentidadRepository;
      this.passwordEncoder = passwordEncoder;
   }

   public UsuarioDto convertirADto(Usuario usuario) {
      return new UsuarioDto(
            usuario.getUsu_id(),
            usuario.getUsu_numrun(),
            usuario.getUsu_dvrun(),
            usuario.getUsu_pri_nombre(),
            usuario.getUsu_seg_nombre(),
            usuario.getUsu_pri_apellido(),
            usuario.getUsu_seg_apellido(),
            usuario.getUsu_email(),
            usuario.getUsu_prom_rep(),
            usuario.isUsu_activo(),
            verificacionIdentidadRepository.findUltimaByUsuario(usuario.getUsu_id())
                  .map(verificacion -> verificacion.getVer_estado() == EstadoVerificacion.APROBADA)
                  .orElse(false));
   }

   public List<UsuarioDto> obtenerTodos(){
      return usuarioRepository.findAll()
            .stream()
            .map(this::convertirADto)
            .toList();
   }

   public UsuarioDto obtenerPorId(int idUsuario){
      Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
      if(usuario == null){
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      } 
      return convertirADto(usuario);
   }

   public UsuarioDto crearUsuario(CrearUsuario nuevo){
      if (usuarioRepository.existsByUsuEmailIgnoreCase(nuevo.getUsu_email())) {
         throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya esta registrado");
      }

      Usuario nuevoUsuario = new Usuario();
      nuevoUsuario.setUsu_numrun(nuevo.getUsu_numrun());
      nuevoUsuario.setUsu_dvrun(nuevo.getUsu_dvrun());
      nuevoUsuario.setUsu_pri_nombre(nuevo.getUsu_pri_nombre());
      nuevoUsuario.setUsu_seg_nombre(nuevo.getUsu_seg_nombre());
      nuevoUsuario.setUsu_pri_apellido(nuevo.getUsu_pri_apellido());
      nuevoUsuario.setUsu_seg_apellido(nuevo.getUsu_seg_apellido());
      nuevoUsuario.setUsu_email(nuevo.getUsu_email());
      nuevoUsuario.setUsu_pass(passwordEncoder.encode(nuevo.getUsu_pass()));
      nuevoUsuario.setUsu_prom_rep(nuevo.getUsu_prom_rep() != null ? nuevo.getUsu_prom_rep() : 0);
      nuevoUsuario.setUsu_activo(nuevo.getUsu_activo() == null || nuevo.getUsu_activo());

      return convertirADto(usuarioRepository.save(nuevoUsuario));
   }

   public String eliminarUsuario(int idUsuario){
      Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
      if(usuario == null){
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }

      usuario.setUsu_activo(false);
      usuarioRepository.save(usuario);
      return "Usuario desactivado correctamente";
   }

   public UsuarioDto actualizarUsuario(Integer idUsuario, ActualizarUsuario nuevo){
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
         if (StringUtils.hasText(nuevo.getUsu_pass())) {
            usuario.setUsu_pass(passwordEncoder.encode(nuevo.getUsu_pass()));
         }
         if (nuevo.getUsu_prom_rep() != null) {
            usuario.setUsu_prom_rep(nuevo.getUsu_prom_rep());
         }
         if (nuevo.getUsu_activo() != null) {
            usuario.setUsu_activo(nuevo.getUsu_activo());
         }
         return convertirADto(usuarioRepository.save(usuario));
      }
   }

}

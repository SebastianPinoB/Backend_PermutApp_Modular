package com.example.PermutApp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.Entities.VerificacionIdentidad.EstadoVerificacion;
import com.example.PermutApp.model.dto.UsuarioDto;
import com.example.PermutApp.model.request.ActualizarUsuario;
import com.example.PermutApp.model.request.CrearUsuario;
import com.example.PermutApp.repository.UsuarioRepository;
import com.example.PermutApp.repository.PasswordRecoveryTokenRepository;
import com.example.PermutApp.repository.VerificacionIdentidadRepository;

@Service
public class UsuarioService {

   private final UsuarioRepository usuarioRepository;
   private final VerificacionIdentidadRepository verificacionIdentidadRepository;
   private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;
   private final PasswordEncoder passwordEncoder;

   public UsuarioService(
         UsuarioRepository usuarioRepository,
         VerificacionIdentidadRepository verificacionIdentidadRepository,
         PasswordRecoveryTokenRepository passwordRecoveryTokenRepository,
         PasswordEncoder passwordEncoder) {
      this.usuarioRepository = usuarioRepository;
      this.verificacionIdentidadRepository = verificacionIdentidadRepository;
      this.passwordRecoveryTokenRepository = passwordRecoveryTokenRepository;
      this.passwordEncoder = passwordEncoder;
   }

   public UsuarioDto convertirADto(Usuario usuario) {
      if (!usuario.isUsu_activo()) {
         return new UsuarioDto(
               usuario.getUsu_id(),
               0,
               '0',
               "Usuario",
               null,
               "eliminado",
               null,
               "usuario.eliminado@permutapp.invalid",
               0,
               false,
               false);
      }
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

   @Transactional
   public String eliminarUsuario(int idUsuario){
      Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
      if(usuario == null){
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
      }

      verificacionIdentidadRepository.eliminarPorUsuario(idUsuario);
      passwordRecoveryTokenRepository.eliminarPorUsuario(idUsuario);

      usuario.setUsu_numrun(100_000_000 + idUsuario);
      usuario.setUsu_dvrun('0');
      usuario.setUsu_pri_nombre("Usuario");
      usuario.setUsu_seg_nombre(null);
      usuario.setUsu_pri_apellido("eliminado");
      usuario.setUsu_seg_apellido(null);
      usuario.setUsu_email("cuenta-eliminada-" + idUsuario + "@permutapp.invalid");
      usuario.setUsu_pass(passwordEncoder.encode(UUID.randomUUID().toString()));
      usuario.setUsu_prom_rep(0);
      usuario.setUsu_activo(false);
      usuarioRepository.save(usuario);
      return "Cuenta eliminada correctamente";
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

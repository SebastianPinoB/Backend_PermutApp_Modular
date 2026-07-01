package com.example.PermutApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.dto.UsuarioDto;
import com.example.PermutApp.repository.PasswordRecoveryTokenRepository;
import com.example.PermutApp.repository.UsuarioRepository;
import com.example.PermutApp.repository.VerificacionIdentidadRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
   @Mock private UsuarioRepository usuarioRepository;
   @Mock private VerificacionIdentidadRepository verificacionIdentidadRepository;
   @Mock private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

   private PasswordEncoder passwordEncoder;
   private UsuarioService usuarioService;

   @BeforeEach
   void setUp() {
      passwordEncoder = new BCryptPasswordEncoder();
      usuarioService = new UsuarioService(
            usuarioRepository,
            verificacionIdentidadRepository,
            passwordRecoveryTokenRepository,
            passwordEncoder);
   }

   @Test
   void eliminarUsuarioAnonimizaDatosYEliminaDatosSensibles() {
      Usuario usuario = usuarioActivo();
      String emailOriginal = usuario.getUsu_email();
      String passwordOriginal = usuario.getUsu_pass();
      when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario));

      String resultado = usuarioService.eliminarUsuario(7);

      assertEquals("Cuenta eliminada correctamente", resultado);
      assertFalse(usuario.isUsu_activo());
      assertEquals("Usuario", usuario.getUsu_pri_nombre());
      assertEquals("eliminado", usuario.getUsu_pri_apellido());
      assertNotEquals(emailOriginal, usuario.getUsu_email());
      assertNotEquals(passwordOriginal, usuario.getUsu_pass());
      verify(verificacionIdentidadRepository).eliminarPorUsuario(7);
      verify(passwordRecoveryTokenRepository).eliminarPorUsuario(7);
      verify(usuarioRepository).save(usuario);
   }

   @Test
   void convertirADtoNoExponeDatosDeUnaCuentaInactiva() {
      Usuario usuario = usuarioActivo();
      usuario.setUsu_activo(false);

      UsuarioDto dto = usuarioService.convertirADto(usuario);

      assertEquals("Usuario", dto.usu_pri_nombre());
      assertEquals("eliminado", dto.usu_pri_apellido());
      assertEquals("usuario.eliminado@permutapp.invalid", dto.usu_email());
      assertFalse(dto.usu_activo());
   }

   private Usuario usuarioActivo() {
      Usuario usuario = new Usuario();
      usuario.setUsu_id(7);
      usuario.setUsu_numrun(12345678);
      usuario.setUsu_dvrun('5');
      usuario.setUsu_pri_nombre("Ana");
      usuario.setUsu_seg_nombre("Maria");
      usuario.setUsu_pri_apellido("Perez");
      usuario.setUsu_seg_apellido("Soto");
      usuario.setUsu_email("ana@email.com");
      usuario.setUsu_pass(passwordEncoder.encode("Password!1"));
      usuario.setUsu_prom_rep(5);
      usuario.setUsu_activo(true);
      return usuario;
   }
}

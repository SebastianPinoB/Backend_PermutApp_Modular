package com.example.PermutApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.auth.AuthResponse;
import com.example.PermutApp.model.auth.LoginRequest;
import com.example.PermutApp.model.auth.RegisterRequest;
import com.example.PermutApp.repository.UsuarioRepository;
import com.example.PermutApp.repository.PasswordRecoveryTokenRepository;
import com.example.PermutApp.repository.VerificacionIdentidadRepository;
import com.example.PermutApp.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

   @Mock
   private UsuarioRepository usuarioRepository;

   @Mock
   private AuthenticationManager authenticationManager;

   @Mock
   private JwtService jwtService;

   @Mock
   private VerificacionIdentidadRepository verificacionIdentidadRepository;

   @Mock
   private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

   private PasswordEncoder passwordEncoder;
   private AuthService authService;

   @BeforeEach
   void setUp() {
      passwordEncoder = new BCryptPasswordEncoder();
      UsuarioService usuarioService = new UsuarioService(
            usuarioRepository,
            verificacionIdentidadRepository,
            passwordRecoveryTokenRepository,
            passwordEncoder);
      authService = new AuthService(usuarioRepository, passwordEncoder, authenticationManager, jwtService, usuarioService);
   }

   @Test
   void registrarHasheaPasswordYDevuelveToken() {
      RegisterRequest request = new RegisterRequest(
            12345678,
            "K",
            "Juan",
            null,
            "Perez",
            null,
            "Juan@Email.com",
            "Password!1");

      when(usuarioRepository.existsByUsuEmailIgnoreCase("Juan@Email.com")).thenReturn(false);
      when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
         Usuario usuario = invocation.getArgument(0);
         usuario.setUsu_id(1);
         return usuario;
      });
      when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt-token");
      when(jwtService.getExpirationMs()).thenReturn(86400000L);

      AuthResponse response = authService.registrar(request);

      ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
      verify(usuarioRepository).save(captor.capture());
      Usuario guardado = captor.getValue();

      assertEquals("jwt-token", response.token());
      assertEquals("juan@email.com", guardado.getUsu_email());
      assertFalse("Password!1".equals(guardado.getUsu_pass()));
      assertTrue(passwordEncoder.matches("Password!1", guardado.getUsu_pass()));
      assertEquals(1, response.usuario().usu_id());
   }

   @Test
   void registrarFallaSiEmailExiste() {
      RegisterRequest request = new RegisterRequest(
            12345678,
            "1",
            "Ana",
            null,
            "Lopez",
            null,
            "ana@email.com",
            "Password!1");
      when(usuarioRepository.existsByUsuEmailIgnoreCase("ana@email.com")).thenReturn(true);

      ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.registrar(request));

      assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
   }

   @Test
   void loginExitosoDevuelveToken() {
      Usuario usuario = usuario("demo@email.com");
      when(usuarioRepository.findByUsuEmailIgnoreCase("demo@email.com")).thenReturn(Optional.of(usuario));
      when(jwtService.generarToken(usuario)).thenReturn("jwt-token");
      when(jwtService.getExpirationMs()).thenReturn(86400000L);

      AuthResponse response = authService.login(new LoginRequest("demo@email.com", "Password!1"));

      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      assertEquals("jwt-token", response.token());
      assertEquals("demo@email.com", response.usuario().usu_email());
   }

   @Test
   void loginFallaConCredencialesInvalidas() {
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("bad credentials"));

      ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> authService.login(new LoginRequest("demo@email.com", "wrong")));

      assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
   }

   private Usuario usuario(String email) {
      Usuario usuario = new Usuario();
      usuario.setUsu_id(1);
      usuario.setUsu_numrun(12345678);
      usuario.setUsu_dvrun('K');
      usuario.setUsu_pri_nombre("Demo");
      usuario.setUsu_pri_apellido("User");
      usuario.setUsu_email(email);
      usuario.setUsu_pass(passwordEncoder.encode("Password!1"));
      usuario.setUsu_prom_rep(0);
      usuario.setUsu_activo(true);
      return usuario;
   }
}

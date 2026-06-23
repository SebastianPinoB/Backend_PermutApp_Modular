package com.example.PermutApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.PasswordRecoveryToken;
import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.auth.PasswordRecoveryResponse;
import com.example.PermutApp.model.auth.ResetPasswordRequest;
import com.example.PermutApp.repository.PasswordRecoveryTokenRepository;
import com.example.PermutApp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

   @Mock
   private IdentidadService identidadService;

   @Mock
   private PasswordRecoveryTokenRepository tokenRepository;

   @Mock
   private UsuarioRepository usuarioRepository;

   private PasswordEncoder passwordEncoder;
   private PasswordRecoveryService recoveryService;
   private MockMultipartFile carnet;
   private MockMultipartFile selfie;

   @BeforeEach
   void setUp() {
      passwordEncoder = new BCryptPasswordEncoder();
      recoveryService = new PasswordRecoveryService(
            identidadService,
            tokenRepository,
            usuarioRepository,
            passwordEncoder);
      carnet = new MockMultipartFile("carnet", "carnet.jpg", "image/jpeg", new byte[] { 1 });
      selfie = new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", new byte[] { 2 });
   }

   @Test
   void verificarIdentidadEmiteTokenHasheadoDeDiezMinutos() {
      Usuario usuario = usuario();
      when(identidadService.validarIdentidadParaRecuperacion("demo@email.com", carnet, selfie))
            .thenReturn(usuario);
      when(tokenRepository.findActiveByUsuarioId(1)).thenReturn(List.of());
      when(tokenRepository.save(any(PasswordRecoveryToken.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

      PasswordRecoveryResponse response = recoveryService.verificarIdentidad(
            "Demo@Email.com",
            carnet,
            selfie,
            "127.0.0.1");

      ArgumentCaptor<PasswordRecoveryToken> captor = ArgumentCaptor.forClass(PasswordRecoveryToken.class);
      verify(tokenRepository).save(captor.capture());
      PasswordRecoveryToken saved = captor.getValue();

      assertFalse(response.recoveryToken().isBlank());
      assertEquals(600, response.expiresIn());
      assertEquals(64, saved.getPrt_token_hash().length());
      assertFalse(saved.getPrt_token_hash().equals(response.recoveryToken()));
      assertTrue(saved.getPrt_expires_at().isAfter(Instant.now().plusSeconds(590)));
   }

   @Test
   void restablecerPasswordConsumeTokenYGuardaNuevoHash() {
      Usuario usuario = usuario();
      PasswordRecoveryToken token = validToken(usuario.getUsu_id());
      when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
      when(usuarioRepository.findById(usuario.getUsu_id())).thenReturn(Optional.of(usuario));

      String response = recoveryService.restablecerPassword(
            new ResetPasswordRequest("raw-token", "NuevaPassword!2"));

      assertEquals("Contrasena actualizada correctamente", response);
      assertTrue(passwordEncoder.matches("NuevaPassword!2", usuario.getUsu_pass()));
      assertTrue(token.isPrt_used());
      verify(usuarioRepository).save(usuario);
      verify(tokenRepository).save(token);
   }

   @Test
   void restablecerPasswordRechazaTokenVencido() {
      PasswordRecoveryToken token = validToken(1);
      token.setPrt_expires_at(Instant.now().minusSeconds(1));
      when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

      ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> recoveryService.restablecerPassword(
                  new ResetPasswordRequest("raw-token", "NuevaPassword!2")));

      assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
      verify(usuarioRepository, never()).save(any(Usuario.class));
   }

   private Usuario usuario() {
      Usuario usuario = new Usuario();
      usuario.setUsu_id(1);
      usuario.setUsu_email("demo@email.com");
      usuario.setUsu_pass(passwordEncoder.encode("Password!1"));
      usuario.setUsu_activo(true);
      return usuario;
   }

   private PasswordRecoveryToken validToken(int usuarioId) {
      PasswordRecoveryToken token = new PasswordRecoveryToken();
      token.setUsu_id(usuarioId);
      token.setPrt_token_hash("hash");
      token.setPrt_expires_at(Instant.now().plusSeconds(600));
      token.setPrt_created_at(Instant.now());
      token.setPrt_used(false);
      return token;
   }
}

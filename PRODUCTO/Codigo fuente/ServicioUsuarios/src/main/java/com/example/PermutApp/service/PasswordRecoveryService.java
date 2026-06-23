package com.example.PermutApp.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.PasswordRecoveryToken;
import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.auth.PasswordRecoveryResponse;
import com.example.PermutApp.model.auth.ResetPasswordRequest;
import com.example.PermutApp.repository.PasswordRecoveryTokenRepository;
import com.example.PermutApp.repository.UsuarioRepository;

@Service
public class PasswordRecoveryService {

   private static final Duration TOKEN_TTL = Duration.ofMinutes(10);
   private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(15);
   private static final int MAX_ATTEMPTS = 3;
   private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

   private final IdentidadService identidadService;
   private final PasswordRecoveryTokenRepository tokenRepository;
   private final UsuarioRepository usuarioRepository;
   private final PasswordEncoder passwordEncoder;
   private final SecureRandom secureRandom = new SecureRandom();
   private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

   public PasswordRecoveryService(
         IdentidadService identidadService,
         PasswordRecoveryTokenRepository tokenRepository,
         UsuarioRepository usuarioRepository,
         PasswordEncoder passwordEncoder) {
      this.identidadService = identidadService;
      this.tokenRepository = tokenRepository;
      this.usuarioRepository = usuarioRepository;
      this.passwordEncoder = passwordEncoder;
   }

   @Transactional
   public PasswordRecoveryResponse verificarIdentidad(
         String email,
         MultipartFile carnet,
         MultipartFile selfie,
         String clientAddress) {
      String normalizedEmail = normalizeEmail(email);
      checkRateLimit("email:" + normalizedEmail);
      checkRateLimit("ip:" + normalizeClientAddress(clientAddress));

      try {
         Usuario usuario = identidadService.validarIdentidadParaRecuperacion(normalizedEmail, carnet, selfie);
         invalidatePreviousTokens(usuario.getUsu_id());

         String rawToken = generateToken();
         PasswordRecoveryToken token = new PasswordRecoveryToken();
         token.setUsu_id(usuario.getUsu_id());
         token.setPrt_token_hash(hashToken(rawToken));
         token.setPrt_expires_at(Instant.now().plus(TOKEN_TTL));
         token.setPrt_used(false);
         token.setPrt_created_at(Instant.now());
         tokenRepository.save(token);

         clearAttempts("email:" + normalizedEmail);
         return new PasswordRecoveryResponse(
               rawToken,
               TOKEN_TTL.toSeconds(),
               "Identidad confirmada. Ahora puedes crear una nueva contrasena.");
      } catch (ResponseStatusException exception) {
         registerFailure("email:" + normalizedEmail);
         registerFailure("ip:" + normalizeClientAddress(clientAddress));
         throw exception;
      }
   }

   @Transactional
   public String restablecerPassword(ResetPasswordRequest request) {
      PasswordRecoveryToken token = tokenRepository.findByTokenHash(hashToken(request.recoveryToken()))
            .orElseThrow(this::invalidToken);

      if (token.isPrt_used() || token.getPrt_expires_at().isBefore(Instant.now())) {
         throw invalidToken();
      }

      Usuario usuario = usuarioRepository.findById(token.getUsu_id())
            .orElseThrow(this::invalidToken);
      if (!usuario.isUsu_activo()) {
         throw invalidToken();
      }
      if (passwordEncoder.matches(request.newPassword(), usuario.getUsu_pass())) {
         throw new ResponseStatusException(
               HttpStatus.BAD_REQUEST,
               "La nueva contrasena debe ser distinta de la anterior");
      }

      usuario.setUsu_pass(passwordEncoder.encode(request.newPassword()));
      usuarioRepository.save(usuario);
      token.setPrt_used(true);
      token.setPrt_used_at(Instant.now());
      tokenRepository.save(token);
      return "Contrasena actualizada correctamente";
   }

   private void invalidatePreviousTokens(int usuarioId) {
      Instant now = Instant.now();
      tokenRepository.findActiveByUsuarioId(usuarioId).forEach(token -> {
         token.setPrt_used(true);
         token.setPrt_used_at(now);
         tokenRepository.save(token);
      });
   }

   private String generateToken() {
      byte[] bytes = new byte[32];
      secureRandom.nextBytes(bytes);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
   }

   private String hashToken(String token) {
      try {
         byte[] digest = MessageDigest.getInstance("SHA-256")
               .digest(token.getBytes(StandardCharsets.UTF_8));
         return java.util.HexFormat.of().formatHex(digest);
      } catch (NoSuchAlgorithmException exception) {
         throw new IllegalStateException("SHA-256 no disponible", exception);
      }
   }

   private String normalizeEmail(String email) {
      if (email == null || email.isBlank() || email.length() > 254
            || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un correo valido");
      }
      return email.trim().toLowerCase(Locale.ROOT);
   }

   private String normalizeClientAddress(String clientAddress) {
      return clientAddress == null || clientAddress.isBlank() ? "unknown" : clientAddress.trim();
   }

   private void checkRateLimit(String key) {
      AttemptWindow window = attempts.get(key);
      if (window != null && window.expiresAt().isAfter(Instant.now()) && window.failures() >= MAX_ATTEMPTS) {
         throw new ResponseStatusException(
               HttpStatus.TOO_MANY_REQUESTS,
               "Demasiados intentos. Espera 15 minutos antes de volver a probar.");
      }
   }

   private void registerFailure(String key) {
      Instant now = Instant.now();
      attempts.compute(key, (ignored, current) -> {
         if (current == null || current.expiresAt().isBefore(now)) {
            return new AttemptWindow(1, now.plus(ATTEMPT_WINDOW));
         }
         return new AttemptWindow(current.failures() + 1, current.expiresAt());
      });
   }

   private void clearAttempts(String key) {
      attempts.remove(key);
   }

   private ResponseStatusException invalidToken() {
      return new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "La autorizacion de recuperacion es invalida o ya vencio");
   }

   private record AttemptWindow(int failures, Instant expiresAt) {
   }
}

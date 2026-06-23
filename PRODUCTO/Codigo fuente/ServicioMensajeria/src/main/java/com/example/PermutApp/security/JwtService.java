package com.example.PermutApp.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JwtService {

   private static final String HMAC_ALGORITHM = "HmacSHA256";
   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

   private final String jwtSecret;

   public JwtService(@Value("${security.jwt.secret}") String jwtSecret) {
      if (jwtSecret == null || jwtSecret.length() < 32) {
         throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres");
      }
      this.jwtSecret = jwtSecret;
   }

   public String obtenerEmail(String token) {
      Object subject = decodificarPayloadComoMap(token).get("sub");
      if (!(subject instanceof String email) || email.isBlank()) {
         throw new IllegalArgumentException("Claim sub no encontrado");
      }
      return email;
   }

   public int obtenerUsuarioId(String token) {
      long uid = obtenerLongClaim(decodificarPayloadComoMap(token), "uid");
      if (uid < 1 || uid > Integer.MAX_VALUE) {
         throw new IllegalArgumentException("Claim uid invalido");
      }
      return (int) uid;
   }

   public boolean esTokenValido(String token) {
      // Valida firma y expiracion sin depender de librerias JWT externas.
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
         return false;
      }

      String unsignedToken = parts[0] + "." + parts[1];
      if (!constantTimeEquals(firmar(unsignedToken), parts[2])) {
         return false;
      }

      return obtenerLongClaim(decodificarPayloadComoMap(token), "exp") > Instant.now().getEpochSecond();
   }

   private String decodificarPayload(String token) {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
         throw new IllegalArgumentException("JWT invalido");
      }
      return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
   }

   private Map<String, Object> decodificarPayloadComoMap(String token) {
      try {
         return OBJECT_MAPPER.readValue(decodificarPayload(token), new TypeReference<>() { });
      } catch (Exception e) {
         throw new IllegalArgumentException("JWT payload invalido", e);
      }
   }

   private long obtenerLongClaim(Map<String, Object> claims, String key) {
      Object value = claims.get(key);
      if (value instanceof Number number) {
         return number.longValue();
      }
      if (value instanceof String text) {
         return Long.parseLong(text);
      }
      throw new IllegalArgumentException("Claim no encontrado: " + key);
   }

   private String firmar(String data) {
      try {
         Mac mac = Mac.getInstance(HMAC_ALGORITHM);
         mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
         return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
      } catch (Exception e) {
         throw new IllegalStateException("No fue posible validar el JWT", e);
      }
   }

   private boolean constantTimeEquals(String left, String right) {
      byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
      byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
      if (leftBytes.length != rightBytes.length) {
         return false;
      }
      int result = 0;
      for (int i = 0; i < leftBytes.length; i++) {
         result |= leftBytes[i] ^ rightBytes[i];
      }
      return result == 0;
   }
}

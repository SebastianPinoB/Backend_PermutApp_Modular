package com.example.PermutApp.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

   private static final String HMAC_ALGORITHM = "HmacSHA256";

   private final String jwtSecret;

   public JwtService(@Value("${security.jwt.secret}") String jwtSecret) {
      if (jwtSecret == null || jwtSecret.length() < 32) {
         throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres");
      }
      this.jwtSecret = jwtSecret;
   }

   public String obtenerEmail(String token) {
      return extraerString(decodificarPayload(token), "sub");
   }

   public boolean esTokenValido(String token) {
      // Valida firma y expiracion sin depender de librerias externas.
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
         return false;
      }

      String unsignedToken = parts[0] + "." + parts[1];
      if (!constantTimeEquals(firmar(unsignedToken), parts[2])) {
         return false;
      }

      return extraerLong(decodificarPayload(token), "exp") > Instant.now().getEpochSecond();
   }

   private String decodificarPayload(String token) {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
         throw new IllegalArgumentException("JWT invalido");
      }
      return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
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

   private String extraerString(String json, String key) {
      String pattern = "\"" + key + "\":\"";
      int start = json.indexOf(pattern);
      if (start < 0) {
         throw new IllegalArgumentException("Claim no encontrado: " + key);
      }
      start += pattern.length();
      int end = json.indexOf("\"", start);
      return json.substring(start, end);
   }

   private long extraerLong(String json, String key) {
      String pattern = "\"" + key + "\":";
      int start = json.indexOf(pattern);
      if (start < 0) {
         throw new IllegalArgumentException("Claim no encontrado: " + key);
      }
      start += pattern.length();
      int end = start;
      while (end < json.length() && Character.isDigit(json.charAt(end))) {
         end++;
      }
      return Long.parseLong(json.substring(start, end));
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

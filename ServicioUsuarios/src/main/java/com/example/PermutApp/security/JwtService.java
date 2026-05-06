package com.example.PermutApp.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.PermutApp.model.Entities.Usuario;

@Service
public class JwtService {

   private static final String HMAC_ALGORITHM = "HmacSHA256";

   private final String jwtSecret;
   private final long expirationMs;

   public JwtService(
         @Value("${security.jwt.secret}") String jwtSecret,
         @Value("${security.jwt.expiration-ms}") long expirationMs) {
      if (jwtSecret == null || jwtSecret.length() < 32) {
         throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres");
      }
      this.jwtSecret = jwtSecret;
      this.expirationMs = expirationMs;
   }

   public String generarToken(Usuario usuario) {
      long now = Instant.now().getEpochSecond();
      long expiresAt = now + (expirationMs / 1000);

      String header = json(Map.of("alg", "HS256", "typ", "JWT"));
      String payload = json(new LinkedHashMap<>() {{
         put("sub", usuario.getUsu_email());
         put("uid", usuario.getUsu_id());
         put("iat", now);
         put("exp", expiresAt);
      }});

      String unsignedToken = base64Url(header.getBytes(StandardCharsets.UTF_8))
            + "."
            + base64Url(payload.getBytes(StandardCharsets.UTF_8));

      return unsignedToken + "." + firmar(unsignedToken);
   }

   public String obtenerEmail(String token) {
      String payload = decodificarPayload(token);
      return extraerString(payload, "sub");
   }

   public boolean esTokenValido(String token) {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
         return false;
      }

      String unsignedToken = parts[0] + "." + parts[1];
      if (!constantTimeEquals(firmar(unsignedToken), parts[2])) {
         return false;
      }

      long exp = extraerLong(decodificarPayload(token), "exp");
      return exp > Instant.now().getEpochSecond();
   }

   public long getExpirationMs() {
      return expirationMs;
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
         return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
      } catch (Exception e) {
         throw new IllegalStateException("No fue posible firmar el JWT", e);
      }
   }

   private String base64Url(byte[] bytes) {
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
   }

   private String json(Map<String, ?> values) {
      StringBuilder builder = new StringBuilder("{");
      boolean first = true;
      for (Map.Entry<String, ?> entry : values.entrySet()) {
         if (!first) {
            builder.append(",");
         }
         first = false;
         builder.append("\"").append(entry.getKey()).append("\":");
         Object value = entry.getValue();
         if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
         } else {
            builder.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
         }
      }
      return builder.append("}").toString();
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

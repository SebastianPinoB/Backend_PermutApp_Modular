package com.example.PermutApp.service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.apache.http.HttpResponse;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;

@Component
public class WebPushSender {
   private static final Logger log = LoggerFactory.getLogger(WebPushSender.class);
   private final String publicKey;
   private final String privateKey;
   private final String subject;

   public WebPushSender(
         @Value("${notifications.vapid.public-key:}") String publicKey,
         @Value("${notifications.vapid.private-key:}") String privateKey,
         @Value("${notifications.vapid.subject:mailto:soporte@permutapp.cl}") String subject) {
      this.publicKey = publicKey;
      this.privateKey = privateKey;
      this.subject = subject;
      if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
         Security.addProvider(new BouncyCastleProvider());
      }
   }

   public boolean estaConfigurado() {
      return !publicKey.isBlank() && !privateKey.isBlank();
   }

   public ResultadoEnvioPush enviar(String endpoint, String p256dh, String auth, String payload) {
      if (!estaConfigurado()) {
         log.warn("Web Push no esta configurado: faltan claves VAPID");
         return ResultadoEnvioPush.ERROR_TEMPORAL;
      }
      if (p256dh == null || auth == null) {
         return ResultadoEnvioPush.SUSCRIPCION_INVALIDA;
      }
      try {
         PushService pushService = new PushService();
         pushService.setSubject(subject);
         pushService.setPublicKey(Utils.loadPublicKey(normalizarBase64(publicKey)));
         pushService.setPrivateKey(Utils.loadPrivateKey(normalizarBase64(privateKey)));
         Notification notification = new Notification(
               endpoint,
               cargarClaveUsuario(p256dh),
               decodificarBase64Url(auth),
               payload.getBytes(StandardCharsets.UTF_8));
         HttpResponse response = pushService.send(notification);
         int status = response.getStatusLine().getStatusCode();
         if (status >= 200 && status < 300) return ResultadoEnvioPush.ENVIADA;
         if (status == 404 || status == 410) return ResultadoEnvioPush.SUSCRIPCION_INVALIDA;
         log.warn("Web Push respondio {} para {}", status, endpoint);
         return ResultadoEnvioPush.ERROR_TEMPORAL;
      } catch (IllegalArgumentException e) {
         log.warn("Suscripcion Web Push invalida para {}: {}", endpoint, e.getMessage());
         return ResultadoEnvioPush.SUSCRIPCION_INVALIDA;
      } catch (Exception e) {
         log.warn("Error enviando Web Push a {}: {}", endpoint, e.getMessage());
         return ResultadoEnvioPush.ERROR_TEMPORAL;
      }
   }

   private PublicKey cargarClaveUsuario(String encoded) throws Exception {
      byte[] bytes = decodificarBase64Url(encoded);
      ECPoint point = ECNamedCurveTable.getParameterSpec("secp256r1").getCurve().decodePoint(bytes);
      ECPublicKeySpec spec = new ECPublicKeySpec(point, ECNamedCurveTable.getParameterSpec("secp256r1"));
      try {
         return KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME).generatePublic(spec);
      } catch (InvalidKeySpecException e) {
         throw new IllegalArgumentException("Clave web push invalida", e);
      }
   }

   private byte[] decodificarBase64Url(String value) {
      return Base64.getUrlDecoder().decode(agregarPadding(value));
   }

   private String normalizarBase64(String value) {
      return agregarPadding(value.replace('-', '+').replace('_', '/'));
   }

   private String agregarPadding(String value) {
      return value + "=".repeat((4 - value.length() % 4) % 4);
   }
}

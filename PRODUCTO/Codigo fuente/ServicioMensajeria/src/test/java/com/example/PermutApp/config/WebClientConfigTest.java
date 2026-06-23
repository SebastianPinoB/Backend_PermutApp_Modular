package com.example.PermutApp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class WebClientConfigTest {
   private HttpServer server;

   @AfterEach
   void tearDown() {
      if (server != null) {
         server.stop(0);
      }
   }

   @Test
   void productoWebClientAceptaImagenesBase64MayoresAlLimitePredeterminado() throws IOException {
      String responseBody = "x".repeat(300_000);
      server = HttpServer.create(new InetSocketAddress(0), 0);
      server.createContext("/producto/11", exchange -> {
         byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
         exchange.sendResponseHeaders(200, response.length);
         exchange.getResponseBody().write(response);
         exchange.close();
      });
      server.start();

      String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
      String result = new WebClientConfig()
            .productoWebClient(baseUrl, 10 * 1024 * 1024)
            .get()
            .uri("/producto/11")
            .retrieve()
            .bodyToMono(String.class)
            .block();

      assertEquals(responseBody.length(), result.length());
   }
}

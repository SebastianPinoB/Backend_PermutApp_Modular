package com.example.PermutApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class RunOcrExtractorTest {

   @Test
   void descartaCandidatoConDigitoVerificadorInvalido() {
      Optional<String> resultado = RunOcrExtractor.detectar(
            "REPUBLICA DE CHILE\nN 36.897.028-2\nCEDULA DE IDENTIDAD",
            "12345678-5");

      assertTrue(resultado.isEmpty());
   }

   @Test
   void priorizaRunRegistradoCuandoHayVariosCandidatosValidos() {
      Optional<String> resultado = RunOcrExtractor.detectar(
            "OTRO NUMERO 11.111.111-1\nRUN 12.345.678-5",
            "12345678-5");

      assertEquals(Optional.of("12345678-5"), resultado);
   }

   @Test
   void aceptaPuntosEspaciosYLetraKMinuscula() {
      Optional<String> resultado = RunOcrExtractor.detectar(
            "RUN 36 897 028 - k",
            "36897028-K");

      assertEquals(Optional.of("36897028-K"), resultado);
   }

   @Test
   void mantieneUnRunValidoAunqueNoCoincidaConElRegistrado() {
      Optional<String> resultado = RunOcrExtractor.detectar(
            "RUN 12.345.678-5",
            "11111111-1");

      assertEquals(Optional.of("12345678-5"), resultado);
   }
}

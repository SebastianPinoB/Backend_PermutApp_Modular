package com.example.PermutApp.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RunOcrExtractor {

   private static final Pattern RUN_PATTERN = Pattern.compile(
         "(?<!\\d)(\\d{1,2}(?:[.\\s]?\\d{3}){2})\\s*-?\\s*([0-9Kk])(?!\\d)");

   private RunOcrExtractor() {
   }

   static Optional<String> detectar(String textoOcr, String runRegistrado) {
      if (textoOcr == null || textoOcr.isBlank()) {
         return Optional.empty();
      }

      List<String> candidatos = extraerCandidatosValidos(textoOcr);
      if (candidatos.isEmpty()) {
         return Optional.empty();
      }

      String registradoNormalizado = normalizar(runRegistrado);
      return candidatos.stream()
            .filter(candidato -> normalizar(candidato).equals(registradoNormalizado))
            .findFirst()
            .or(() -> Optional.of(candidatos.getFirst()));
   }

   private static List<String> extraerCandidatosValidos(String textoOcr) {
      Matcher matcher = RUN_PATTERN.matcher(textoOcr);
      Set<String> candidatos = new LinkedHashSet<>();

      while (matcher.find()) {
         String cuerpo = matcher.group(1).replaceAll("\\D", "");
         char dv = Character.toUpperCase(matcher.group(2).charAt(0));
         if (digitoVerificador(cuerpo) == dv) {
            candidatos.add(cuerpo + "-" + dv);
         }
      }

      return new ArrayList<>(candidatos);
   }

   private static char digitoVerificador(String cuerpo) {
      int suma = 0;
      int multiplicador = 2;

      for (int i = cuerpo.length() - 1; i >= 0; i--) {
         suma += Character.digit(cuerpo.charAt(i), 10) * multiplicador;
         multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
      }

      int resultado = 11 - (suma % 11);
      if (resultado == 11) {
         return '0';
      }
      if (resultado == 10) {
         return 'K';
      }
      return Character.forDigit(resultado, 10);
   }

   private static String normalizar(String run) {
      if (run == null) {
         return "";
      }
      return run.replaceAll("[^0-9Kk]", "")
            .toUpperCase(Locale.ROOT);
   }
}

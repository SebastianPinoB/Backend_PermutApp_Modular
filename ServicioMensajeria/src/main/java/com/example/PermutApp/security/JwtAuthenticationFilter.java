package com.example.PermutApp.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

   private final JwtService jwtService;

   public JwtAuthenticationFilter(JwtService jwtService) {
      this.jwtService = jwtService;
   }

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
         throws ServletException, IOException {
      // Cada microservicio valida el mismo JWT emitido por ServicioUsuarios.
      String header = request.getHeader("Authorization");
      if (header == null || !header.startsWith("Bearer ")) {
         filterChain.doFilter(request, response);
         return;
      }

      String token = header.substring(7);
      // Solo guardamos el email del token; estos servicios no necesitan cargar el usuario completo.
      if (jwtService.esTokenValido(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
         String email = jwtService.obtenerEmail(token);
         SecurityContextHolder.getContext().setAuthentication(
               new UsernamePasswordAuthenticationToken(email, null, List.of()));
      }

      filterChain.doFilter(request, response);
   }
}

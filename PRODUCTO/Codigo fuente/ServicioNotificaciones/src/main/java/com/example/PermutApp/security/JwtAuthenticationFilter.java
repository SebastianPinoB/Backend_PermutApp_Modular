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
      String header = request.getHeader("Authorization");
      if (header != null && header.startsWith("Bearer ")) {
         String token = header.substring(7);
         if (jwtService.esTokenValido(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            int usuarioId = jwtService.obtenerUsuarioId(token);
            SecurityContextHolder.getContext().setAuthentication(
                  new UsernamePasswordAuthenticationToken(usuarioId, null, List.of()));
         }
      }
      filterChain.doFilter(request, response);
   }
}

package com.example.PermutApp.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

   private final JwtService jwtService;
   private final UserDetailsService userDetailsService;

   public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
      this.jwtService = jwtService;
      this.userDetailsService = userDetailsService;
   }

   @Override
   protected void doFilterInternal(
         HttpServletRequest request,
         HttpServletResponse response,
         FilterChain filterChain) throws ServletException, IOException {
      String token = obtenerBearerToken(request);

      try {
         if (token != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtService.esTokenValido(token)) {
            String email = jwtService.obtenerEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                  userDetails,
                  null,
                  userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
         }
      } catch (Exception e) {
         SecurityContextHolder.clearContext();
      }

      filterChain.doFilter(request, response);
   }

   private String obtenerBearerToken(HttpServletRequest request) {
      String header = request.getHeader("Authorization");
      if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
         return null;
      }
      return header.substring(7);
   }
}

package com.example.PermutApp.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PermutApp.model.Entities.Usuario;
import com.example.PermutApp.model.auth.AuthResponse;
import com.example.PermutApp.model.auth.LoginRequest;
import com.example.PermutApp.model.auth.RegisterRequest;
import com.example.PermutApp.repository.UsuarioRepository;
import com.example.PermutApp.security.JwtService;

@Service
public class AuthService {

   private final UsuarioRepository usuarioRepository;
   private final PasswordEncoder passwordEncoder;
   private final AuthenticationManager authenticationManager;
   private final JwtService jwtService;
   private final UsuarioService usuarioService;

   public AuthService(
         UsuarioRepository usuarioRepository,
         PasswordEncoder passwordEncoder,
         AuthenticationManager authenticationManager,
         JwtService jwtService,
         UsuarioService usuarioService) {
      this.usuarioRepository = usuarioRepository;
      this.passwordEncoder = passwordEncoder;
      this.authenticationManager = authenticationManager;
      this.jwtService = jwtService;
      this.usuarioService = usuarioService;
   }

   public AuthResponse registrar(RegisterRequest request) {
      if (usuarioRepository.existsByUsuEmailIgnoreCase(request.usu_email())) {
         throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya esta registrado");
      }

      Usuario usuario = new Usuario();
      usuario.setUsu_numrun(request.usu_numrun());
      usuario.setUsu_dvrun(request.usu_dvrun().charAt(0));
      usuario.setUsu_pri_nombre(request.usu_pri_nombre());
      usuario.setUsu_seg_nombre(request.usu_seg_nombre());
      usuario.setUsu_pri_apellido(request.usu_pri_apellido());
      usuario.setUsu_seg_apellido(request.usu_seg_apellido());
      usuario.setUsu_email(request.usu_email().trim().toLowerCase());
      usuario.setUsu_pass(passwordEncoder.encode(request.usu_pass()));
      usuario.setUsu_prom_rep(0);
      usuario.setUsu_activo(true);

      Usuario guardado = usuarioRepository.save(usuario);
      return crearRespuesta(guardado);
   }

   public AuthResponse login(LoginRequest request) {
      try {
         authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(request.email(), request.password()));
      } catch (BadCredentialsException e) {
         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
      }

      Usuario usuario = usuarioRepository.findByUsuEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

      if (!usuario.isUsu_activo()) {
         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta esta desactivada");
      }

      return crearRespuesta(usuario);
   }

   private AuthResponse crearRespuesta(Usuario usuario) {
      return new AuthResponse(
            jwtService.generarToken(usuario),
            "Bearer",
            jwtService.getExpirationMs() / 1000,
            usuarioService.convertirADto(usuario));
   }
}

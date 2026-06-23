package com.example.PermutApp.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.PermutApp.model.auth.AuthResponse;
import com.example.PermutApp.model.auth.LoginRequest;
import com.example.PermutApp.model.auth.PasswordRecoveryResponse;
import com.example.PermutApp.model.auth.RegisterRequest;
import com.example.PermutApp.model.auth.ResetPasswordRequest;
import com.example.PermutApp.service.AuthService;
import com.example.PermutApp.service.PasswordRecoveryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("auth")
public class AuthController {

   private final AuthService authService;
   private final PasswordRecoveryService passwordRecoveryService;

   public AuthController(AuthService authService, PasswordRecoveryService passwordRecoveryService) {
      this.authService = authService;
      this.passwordRecoveryService = passwordRecoveryService;
   }

   @PostMapping("register")
   @ResponseStatus(HttpStatus.CREATED)
   public AuthResponse registrar(@Valid @RequestBody RegisterRequest request) {
      return authService.registrar(request);
   }

   @PostMapping("login")
   public AuthResponse login(@Valid @RequestBody LoginRequest request) {
      return authService.login(request);
   }

   @PostMapping("password-recovery/verify-identity")
   public PasswordRecoveryResponse verificarIdentidadParaRecuperacion(
         @RequestParam("email") String email,
         @RequestParam("carnet") MultipartFile carnet,
         @RequestParam("selfie") MultipartFile selfie,
         HttpServletRequest httpRequest) {
      return passwordRecoveryService.verificarIdentidad(
            email,
            carnet,
            selfie,
            getClientAddress(httpRequest));
   }

   @PostMapping("password-recovery/reset")
   public String restablecerPassword(@Valid @RequestBody ResetPasswordRequest request) {
      return passwordRecoveryService.restablecerPassword(request);
   }

   private String getClientAddress(HttpServletRequest request) {
      String forwardedFor = request.getHeader("X-Forwarded-For");
      if (forwardedFor == null || forwardedFor.isBlank()) {
         return request.getRemoteAddr();
      }
      int separator = forwardedFor.indexOf(',');
      return (separator >= 0 ? forwardedFor.substring(0, separator) : forwardedFor).trim();
   }
}

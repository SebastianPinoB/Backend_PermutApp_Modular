package com.example.PermutApp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.PermutApp.model.auth.AuthResponse;
import com.example.PermutApp.model.auth.LoginRequest;
import com.example.PermutApp.model.auth.RegisterRequest;
import com.example.PermutApp.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("auth")
public class AuthController {

   private final AuthService authService;

   public AuthController(AuthService authService) {
      this.authService = authService;
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
}

package com.example.PermutApp.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.PermutApp.model.auth.AuthResponse;
import com.example.PermutApp.model.auth.LoginRequest;
import com.example.PermutApp.model.auth.RegisterRequest;
import com.example.PermutApp.model.dto.UsuarioDto;
import com.example.PermutApp.service.AuthService;
import com.example.PermutApp.service.PasswordRecoveryService;

class AuthControllerTest {

   @Mock
   private AuthService authService;

   @Mock
   private PasswordRecoveryService passwordRecoveryService;

   private MockMvc mockMvc;

   @BeforeEach
   void setUp() {
      MockitoAnnotations.openMocks(this);
      LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
      validator.afterPropertiesSet();
      mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(authService, passwordRecoveryService))
            .setValidator(validator)
            .build();
   }

   @Test
   void loginNoExponePassword() throws Exception {
      when(authService.login(any(LoginRequest.class))).thenReturn(authResponse());

      mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                    "email": "demo@email.com",
                    "password": "Password!1"
                  }
                  """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.usuario.usu_email").value("demo@email.com"))
            .andExpect(content().string(not(containsString("usu_pass"))));
   }

   @Test
   void registerValidaPayloadInvalido() throws Exception {
      mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                    "usu_email": "correo-invalido",
                    "usu_pass": "123"
                  }
                  """))
            .andExpect(status().isBadRequest());
   }

   @Test
   void registerValidoDevuelveCreated() throws Exception {
      when(authService.registrar(any(RegisterRequest.class))).thenReturn(authResponse());

      mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                    "usu_numrun": 12345678,
                    "usu_dvrun": "K",
                    "usu_pri_nombre": "Demo",
                    "usu_pri_apellido": "User",
                    "usu_email": "demo@email.com",
                    "usu_pass": "Password!1"
                  }
                  """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(content().string(not(containsString("usu_pass"))));
   }

   @Test
   void registerRechazaNombresYCorreoQueSuperanElMaximo() throws Exception {
      mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                    "usu_numrun": 12345678,
                    "usu_dvrun": "K",
                    "usu_pri_nombre": "NombreConMasDe15",
                    "usu_seg_nombre": "SegundoNombreLargo",
                    "usu_pri_apellido": "ApellidoMuyExtenso",
                    "usu_seg_apellido": "SegundoMuyExtenso",
                    "usu_email": "correo.demasiado.largo@email.com",
                    "usu_pass": "Password!1"
                  }
                  """))
            .andExpect(status().isBadRequest());
   }

   private AuthResponse authResponse() {
      return new AuthResponse(
            "jwt-token",
            "Bearer",
            86400,
            new UsuarioDto(1, 12345678, 'K', "Demo", null, "User", null, "demo@email.com", 0, true, false));
   }
}

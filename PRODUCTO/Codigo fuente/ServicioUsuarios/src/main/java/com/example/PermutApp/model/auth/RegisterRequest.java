package com.example.PermutApp.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
      @NotNull @Positive Integer usu_numrun,
      @NotBlank @Size(min = 1, max = 1) String usu_dvrun,
      @NotBlank @Size(max = 15) String usu_pri_nombre,
      @Size(max = 15) String usu_seg_nombre,
      @NotBlank @Size(max = 15) String usu_pri_apellido,
      @Size(max = 15) String usu_seg_apellido,
      @NotBlank @Email @Size(max = 25) String usu_email,
      @NotBlank @Size(min = 8) String usu_pass) {
}

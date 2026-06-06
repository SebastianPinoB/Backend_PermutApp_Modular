package com.example.PermutApp.model.dto;

import com.example.PermutApp.model.entities.CanalNotificacion;

public record SuscripcionDto(int id, CanalNotificacion canal, String plataforma, boolean activa) {
}

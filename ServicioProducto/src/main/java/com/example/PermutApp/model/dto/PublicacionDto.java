package com.example.PermutApp.model.dto;

public record PublicacionDto(
    // La fecha se recibe como texto para tolerar timestamp o ISO desde ServicioPublicaciones.
    int publ_id,
    String publ_titulo,
    String publ_descripcion,
    String publ_fech_creacion,
    Boolean publ_activo,
    int publ_autor_id
){
}

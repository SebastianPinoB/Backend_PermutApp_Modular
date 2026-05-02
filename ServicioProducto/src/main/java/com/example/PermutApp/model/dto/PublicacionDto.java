package com.example.PermutApp.model.dto;

import java.util.Date;

public record PublicacionDto(
    //Los datos en el mismo orden que los arroja la api
    int publ_id,
    String publ_titulo,
    String publ_descripcion,
    Date publ_fech_creacion,
    Boolean publ_activo,
    int publ_autor_id
){

}

package com.example.PermutApp.model.dto;

import java.util.List;

public record ProductoCategoriaDto(
    String cat_id,
    String cat_nombre,
    String cat_query,
    String cat_icon,
    String cat_bg_color,
    String cat_border_color,
    String cat_icon_color,
    List<String> cat_keywords,
    int cat_orden
) {
}

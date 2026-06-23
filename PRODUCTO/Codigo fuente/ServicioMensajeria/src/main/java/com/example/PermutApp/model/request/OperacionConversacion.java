package com.example.PermutApp.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OperacionConversacion(@NotNull @Positive Integer usuario_id) {
}

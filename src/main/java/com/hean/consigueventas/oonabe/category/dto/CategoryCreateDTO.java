package com.hean.consigueventas.oonabe.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String description) {
}

package com.hean.consigueventas.oonabe.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar una categoria.")
public record CategoryCreateDTO(
        @Schema(description = "Nombre publico de la categoria.", example = "Yoga")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String name,
        @Schema(description = "Descripcion breve.", example = "Practicas de yoga y bienestar corporal.")
        @Size(max = 500)
        String description) {
}

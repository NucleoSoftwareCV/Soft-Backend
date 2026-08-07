package com.hean.consigueventas.oonabe.experienceType.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un tipo de experiencia.")
public record ExperienceTypeUpsertRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String name,
        @Size(max = 500)
        String description) {
}

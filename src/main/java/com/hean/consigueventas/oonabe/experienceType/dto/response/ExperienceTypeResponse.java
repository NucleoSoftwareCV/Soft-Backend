package com.hean.consigueventas.oonabe.experienceType.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de experiencia disponible para clasificar eventos.")
public record ExperienceTypeResponse(
        Long id,
        String name,
        String slug,
        String description,
        boolean active,
        boolean deletable) {
}

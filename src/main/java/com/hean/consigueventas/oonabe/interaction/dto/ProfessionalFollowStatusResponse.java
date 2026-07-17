package com.hean.consigueventas.oonabe.interaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de seguimiento de un perfil profesional")
public record ProfessionalFollowStatusResponse(
        @Schema(description = "ID del perfil profesional", example = "1")
        Long professionalId,

        @Schema(description = "Indica si el usuario autenticado sigue al profesional", example = "true")
        boolean following
) {
}

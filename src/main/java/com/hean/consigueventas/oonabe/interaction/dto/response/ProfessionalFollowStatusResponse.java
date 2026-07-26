package com.hean.consigueventas.oonabe.interaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de seguimiento de un profesional")
public record ProfessionalFollowStatusResponse(

        @Schema(description = "ID del perfil profesional")
        Long professionalId,

        @Schema(description = "Indica si el cliente sigue al profesional")
        Boolean following,

        @Schema(description = "Cantidad de seguidores del profesional")
        Long followersCount
) {
}
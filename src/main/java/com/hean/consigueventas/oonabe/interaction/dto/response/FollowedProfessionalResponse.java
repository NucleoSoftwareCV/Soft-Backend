package com.hean.consigueventas.oonabe.interaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Profesional seguido por el cliente")
public record FollowedProfessionalResponse(

        @Schema(description = "ID del perfil profesional")
        Long professionalId,

        @Schema(description = "Slug público del profesional")
        String slug,

        @Schema(description = "Nombre público del profesional")
        String publicName,

        @Schema(description = "Categoría del perfil profesional")
        String profileCategory,

        @Schema(description = "Biografía del profesional")
        String biography,

        @Schema(description = "URL de la foto del profesional")
        String photoUrl,

        @Schema(description = "Fecha en la que el cliente empezó a seguirlo")
        Instant followedAt
) {
}
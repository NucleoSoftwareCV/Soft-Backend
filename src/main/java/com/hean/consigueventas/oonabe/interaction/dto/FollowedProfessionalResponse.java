package com.hean.consigueventas.oonabe.interaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Card minima de un perfil profesional seguido")
public record FollowedProfessionalResponse(
        @Schema(description = "ID del perfil profesional", example = "1")
        Long id,

        @Schema(description = "Identificador publico del perfil", example = "ana-psicologa")
        String slug,

        @Schema(description = "Nombre publico del profesional", example = "Ana Gomez")
        String publicName,

        @Schema(description = "Categoria del perfil", example = "PROFESIONALES")
        String profileCategory,

        @Schema(description = "URL de la foto del profesional")
        String photoUrl,

        @Schema(description = "Fecha en que el usuario comenzo a seguir al profesional")
        Instant followedAt
) {
}

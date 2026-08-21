package com.hean.consigueventas.oonabe.profileProfesional.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los datos de una red social del perfil")
public record ProfessionalSocialLinkResponse(

        @Schema(description = "ID del enlace social")
        Long id,

        @Schema(
                description = "Plataforma de la red social",
                example = "INSTAGRAM"
        )
        String platform,

        @Schema(
                description = "URL del perfil en la red social",
                example = "https://www.instagram.com/perfil"
        )
        String profileUrl

) {
}
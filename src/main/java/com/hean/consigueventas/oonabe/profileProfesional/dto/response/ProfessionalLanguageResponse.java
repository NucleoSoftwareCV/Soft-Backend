package com.hean.consigueventas.oonabe.profileProfesional.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Idioma registrado por el profesional")
public record ProfessionalLanguageResponse(

        @Schema(description = "ID del idioma registrado")
        Long id,

        @Schema(description = "Nombre del idioma")
        String languageName
) {
}
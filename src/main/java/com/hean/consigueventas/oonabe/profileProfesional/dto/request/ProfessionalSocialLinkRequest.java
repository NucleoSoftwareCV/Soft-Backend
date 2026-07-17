package com.hean.consigueventas.oonabe.profileProfesional.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProfessionalSocialLinkRequest(

        @NotBlank(message = "La plataforma es obligatoria")
        String platform,

        @NotBlank(message = "El enlace de la red social es obligatorio")
        String profileUrl

) {
}
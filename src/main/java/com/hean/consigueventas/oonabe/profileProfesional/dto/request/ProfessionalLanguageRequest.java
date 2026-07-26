package com.hean.consigueventas.oonabe.profileProfesional.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfessionalLanguageRequest(

        @NotBlank(message = "El idioma es obligatorio")
        @Size(max = 80, message = "El idioma no puede superar los 80 caracteres")
        String languageName
) {
}
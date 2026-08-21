package com.hean.consigueventas.oonabe.profileProfesional.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SpecialistProfileRequest(

        @NotBlank(message = "El nombre público es obligatorio")
        @Size(max = 150, message = "El nombre público no puede superar los 150 caracteres")
        String publicName,

        @NotBlank(message = "La categoría del perfil es obligatoria")
        @Size(max = 30, message = "La categoría no puede superar los 30 caracteres")
        String profileCategory,

        @NotBlank(message = "La biografía es obligatoria")
        @Size(max = 255, message = "La biografía no puede superar los 255 caracteres")
        String biography,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres")
        String description,

        @NotBlank(message = "El número de WhatsApp es obligatorio")
        @Pattern(
                regexp = "^\\+[0-9]{2} ?[0-9]{9}$",
                message = "WhatsApp debe incluir el prefijo internacional y exactamente 9 dígitos, por ejemplo +51 928037195"
        )
        String whatsappPhone,

        @Pattern(
                regexp = "^$|^[0-9]{9}$",
                message = "El teléfono debe contener exactamente 9 dígitos"
        )
        String phoneNumber,

        @Email(message = "El correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String publicEmail,

        String website,

        Set<Long> workTopicIds,

        Set<Long> techniqueIds
) {
}

package com.hean.consigueventas.oonabe.profileProfesional.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
        String biography,

        @NotBlank(message = "La foto del perfil es obligatoria")
        String photoUrl,

        @NotBlank(message = "El número de WhatsApp es obligatorio")
        @Size(max = 25, message = "El número de WhatsApp no puede superar los 25 caracteres")
        String whatsappPhone,

        @Size(max = 25, message = "El número celular no puede superar los 25 caracteres")
        String phoneNumber,

        @Email(message = "El correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String publicEmail,

        String website,

        Set<Long> workTopicIds,

        Set<Long> techniqueIds
) {
}
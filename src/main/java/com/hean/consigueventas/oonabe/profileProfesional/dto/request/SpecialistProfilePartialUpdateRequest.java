package com.hean.consigueventas.oonabe.profileProfesional.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SpecialistProfilePartialUpdateRequest(

        @Size(
                max = 150,
                message = "El nombre público no puede superar los 150 caracteres"
        )
        String publicName,

        @Size(
                max = 30,
                message = "La categoría no puede superar los 30 caracteres"
        )
        String profileCategory,

        @Size(
                max = 255,
                message = "La biografía no puede superar los 255 caracteres"
        )
        String biography,

        @Size(
                max = 5000,
                message = "La descripción no puede superar los 5000 caracteres"
        )
        String description,

        @Pattern(
                regexp = "^$|^\\+[1-9][0-9]{0,2} ?[0-9]{6,14}$",
                message = "WhatsApp debe estar en formato internacional, por ejemplo +51 985471254 o +34 612345678"
        )
        String whatsappPhone,

        @Pattern(
                regexp = "^$|^[0-9]{9}$",
                message = "El teléfono debe contener exactamente 9 dígitos"
        )
        String phoneNumber,

        @Email(
                message = "El correo electrónico no es válido"
        )
        @Size(
                max = 150,
                message = "El correo no puede superar los 150 caracteres"
        )
        String publicEmail,

        String website,

        Set<Long> workTopicIds,

        Set<Long> techniqueIds,

        Boolean showUpcomingEvents,

        Boolean showOneToOneSessions,

        Boolean showGallery

) {
}
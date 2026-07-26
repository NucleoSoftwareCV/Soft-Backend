package com.hean.consigueventas.oonabe.profileCliente.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ClientProfilePreferencesRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String lastName,

        @NotNull(message = "La ciudad es obligatoria")
        Long cityId,

        @NotBlank(message = "El correo de comunicación es obligatorio")
        @Email(message = "El correo de comunicación no es válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String communicationEmail,

        @Size(max = 25, message = "El número de WhatsApp no puede superar los 25 caracteres")
        String whatsappPhone,

        Boolean receiveSavedEventConfirmations,

        Boolean receivePersonalizedRecommendations,

        Boolean receiveReservationConfirmations,

        Boolean receiveWeeklySummary,

        @NotEmpty(message = "Debe seleccionar al menos una categoría de interés")
        Set<Long> categoryIds
) {
}
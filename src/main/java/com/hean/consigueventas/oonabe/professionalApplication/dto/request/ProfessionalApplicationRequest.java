package com.hean.consigueventas.oonabe.professionalApplication.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Datos para solicitar convertirse en profesional"
)
public record ProfessionalApplicationRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(
                max = 200,
                message = "El nombre no puede superar los 200 caracteres"
        )
        @Schema(example = "Ana Garcia Lopez")
        String fullName,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(
                max = 100,
                message = "La ciudad no puede superar los 100 caracteres"
        )
        @Schema(example = "Lima")
        String city,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(
                max = 150,
                message = "El email no puede superar los 150 caracteres"
        )
        @Schema(example = "ana@gmail.com")
        String email,

        @NotBlank(message = "El tipo profesional es obligatorio")
        @Size(
                max = 50,
                message = "El tipo profesional no puede superar los 50caracteres"
        )
        @Schema(example = "Terapeuta")
        String professionalType,

        @NotBlank(message = "El WhatsApp es obligatorio")
        @Size(
                max = 20,
                message = "El WhatsApp no puede superar los 20 caracteres"
        )
        @Schema(example = "+51 999 999 999")
        String whatsappPhone,

        @NotBlank(message = "La motivación es obligatoria")
        @Size(
                max = 500,
                message = "La motivación no puede superar los 500 caracteres"
        )
        @Schema(example = "Quiero formar parte de Círculo Oona para compartir mis servicios.")
        String motivation

) {
}
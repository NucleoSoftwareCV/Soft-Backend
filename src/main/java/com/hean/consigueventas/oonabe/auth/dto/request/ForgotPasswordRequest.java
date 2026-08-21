package com.hean.consigueventas.oonabe.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de recuperación de contraseña.")
public record ForgotPasswordRequest(
        @Schema(description = "Email de la cuenta.", example = "usuario@example.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email
) {
}

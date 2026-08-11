package com.hean.consigueventas.oonabe.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmación de una operación de recuperación de contraseña.")
public record PasswordResetResponse(
        @Schema(example = "Si el email existe, te enviamos un enlace para restablecer tu contraseña.")
        String message
) {
}

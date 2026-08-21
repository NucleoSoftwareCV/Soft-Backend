package com.hean.consigueventas.oonabe.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Confirmación de recuperación de contraseña con el token recibido por email.")
public record ResetPasswordRequest(
        @Schema(description = "Token de recuperación opaco recibido por email.", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "El token es obligatorio")
        String token,

        @Schema(description = "Nueva contraseña.", example = "Str0ngPass123", minLength = 8, maxLength = 72,
                accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        @Size(min = 8, max = 72)
        String newPassword
) {
}

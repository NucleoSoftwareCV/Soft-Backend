package com.hean.consigueventas.oonabe.auth.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud para renovar o revocar un refresh token.")
public record TokenRefreshRequest(
        @Schema(description = "Refresh token opaco.", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "El token de refresco es obligatorio")
        String refreshToken) {
}

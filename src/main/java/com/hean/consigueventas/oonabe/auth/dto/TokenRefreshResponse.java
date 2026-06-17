package com.hean.consigueventas.oonabe.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de renovacion de access token.")
public record TokenRefreshResponse(
        @Schema(description = "Nuevo access token JWT.")
        String accessToken,
        @Schema(description = "Refresh token usado para la renovacion.")
        String refreshToken,
        @Schema(description = "Tipo de token.", example = "Bearer")
        String tokenType) {
    public TokenRefreshResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}

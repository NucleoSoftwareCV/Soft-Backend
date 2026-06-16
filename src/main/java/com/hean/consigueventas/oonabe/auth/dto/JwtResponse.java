package com.hean.consigueventas.oonabe.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Respuesta de autenticacion con tokens y datos basicos del usuario.")
public record JwtResponse(
        @Schema(description = "Access token JWT.", accessMode = Schema.AccessMode.READ_ONLY)
        String token,
        @Schema(description = "Refresh token opaco.", accessMode = Schema.AccessMode.READ_ONLY)
        String refreshToken,
        @Schema(description = "Tipo de token.", example = "Bearer")
        String type,
        Long id,
        String username,
        String email,
        Set<String> roles) {
    public JwtResponse(String token, String refreshToken, Long id, String username, String email, Set<String> roles) {
        this(token, refreshToken, "Bearer", id, username, email, roles);
    }
}

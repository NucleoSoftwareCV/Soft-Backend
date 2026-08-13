package com.hean.consigueventas.oonabe.auth.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Respuesta de autenticación con tokens y datos básicos del usuario.")
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
        String firstName,
        Set<String> roles,
        boolean onboardingRequired,
        boolean newlyRegistered) {
    public JwtResponse(String token, String refreshToken, Long id, String username, String email, String firstName,
                       Set<String> roles, boolean onboardingRequired, boolean newlyRegistered) {
        this(token, refreshToken, "Bearer", id, username, email, firstName, roles, onboardingRequired, newlyRegistered);
    }
}

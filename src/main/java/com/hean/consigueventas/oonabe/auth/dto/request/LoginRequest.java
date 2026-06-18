package com.hean.consigueventas.oonabe.auth.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de inicio de sesión.")
public record LoginRequest(
        @Schema(description = "Username o email.", example = "usuario@example.com")
        @NotBlank
        String username,
        @Schema(description = "Contraseña.", example = "Str0ngPass123", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        String password) {
}

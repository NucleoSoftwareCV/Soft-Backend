package com.hean.consigueventas.oonabe.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un usuario final.")
public record RegisterRequest(
        @Schema(description = "Nombre de usuario unico.", example = "usuario_oona", minLength = 3, maxLength = 20)
        @NotBlank
        @Size(min = 3, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El username solo puede contener letras, numeros, punto, guion y guion bajo")
        String username,
        @Schema(description = "Email unico del usuario.", example = "usuario@example.com", format = "email", maxLength = 150)
        @NotBlank
        @Size(max = 150)
        @Email
        String email,
        @Schema(description = "Contrasena del usuario.", example = "Str0ngPass123", minLength = 8, maxLength = 72,
                accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}

package com.hean.consigueventas.oonabe.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Datos publicos del usuario autenticado.")
public record UserDTO(
        Long id,
        String username,
        String email,
        Boolean active,
        LocalDateTime disabledAt,
        LocalDateTime createdAt,
        Set<String> roles
) {
}

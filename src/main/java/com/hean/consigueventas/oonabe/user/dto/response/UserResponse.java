package com.hean.consigueventas.oonabe.user.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Datos públicos del usuario autenticado.")
public record UserResponse(
        Long id,
        String username,
        String email,
        Boolean active,
        LocalDateTime disabledAt,
        LocalDateTime createdAt,
        Set<String> roles
) {
}

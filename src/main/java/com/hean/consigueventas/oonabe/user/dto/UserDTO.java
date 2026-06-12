package com.hean.consigueventas.oonabe.user.dto;

import java.time.LocalDateTime;
import java.util.Set;

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

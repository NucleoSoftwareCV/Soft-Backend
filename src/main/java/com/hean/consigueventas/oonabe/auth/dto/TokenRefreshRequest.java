package com.hean.consigueventas.oonabe.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(@NotBlank(message = "El token de refresco es obligatorio") String refreshToken) {
}

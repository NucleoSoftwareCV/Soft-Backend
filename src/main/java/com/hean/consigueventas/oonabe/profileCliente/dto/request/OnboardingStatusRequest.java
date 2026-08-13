package com.hean.consigueventas.oonabe.profileCliente.dto.request;

import com.hean.consigueventas.oonabe.profileCliente.entity.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Estado final solicitado para el onboarding.")
public record OnboardingStatusRequest(
        @NotNull(message = "El estado es obligatorio")
        OnboardingStatus status
) {
}

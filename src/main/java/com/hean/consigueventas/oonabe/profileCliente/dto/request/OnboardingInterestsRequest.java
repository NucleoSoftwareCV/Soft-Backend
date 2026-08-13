package com.hean.consigueventas.oonabe.profileCliente.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

@Schema(description = "Categorías elegidas en el primer paso del onboarding.")
public record OnboardingInterestsRequest(
        @NotEmpty(message = "Debe seleccionar al menos una categoría de interés")
        Set<Long> categoryIds
) {
}

package com.hean.consigueventas.oonabe.profileCliente.dto.request;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Preferencias opcionales de personalización del onboarding.")
public record OnboardingPreferencesRequest(
        Long cityId,
        Set<Long> experienceTypeIds,
        EventModality modality
) {
}

package com.hean.consigueventas.oonabe.profileCliente.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.profileCliente.entity.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Estado y selecciones actuales del onboarding del cliente.")
public record ClientOnboardingResponse(
        OnboardingStatus status,
        Set<Long> categoryIds,
        Long cityId,
        Set<Long> experienceTypeIds,
        EventModality modality
) {
}

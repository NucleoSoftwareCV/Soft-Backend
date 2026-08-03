package com.hean.consigueventas.oonabe.event.dto.request;

import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import jakarta.validation.constraints.NotNull;

public record EventStatusUpdateRequest(
        @NotNull(message = "El estado es obligatorio")
        EventStatus status
) {
}

package com.hean.consigueventas.oonabe.event.dto.request;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import jakarta.validation.constraints.NotNull;

public record EventOccurrenceStatusUpdateRequest(
        @NotNull(message = "El estado es obligatorio")
        EventOccurrenceStatus status
) {
}

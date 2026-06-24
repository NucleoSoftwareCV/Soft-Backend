package com.hean.consigueventas.oonabe.event.dto.request;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EventOccurrenceUpsertRequest(
        @NotNull Long eventId,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt,
        Long locationId,
        @NotNull @Min(0) Integer capacity,
        @Min(0) Integer reservedSpots,
        EventOccurrenceStatus status
//        String virtualUrl
) {
}

package com.hean.consigueventas.oonabe.event.dto;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;

import java.time.Instant;

public record EventOccurrencePublicDTO(
        Long id,
        Long eventId,
        String eventTitle,
        Instant startsAt,
        Instant endsAt,
        String locationName,
        Integer availableSpots,
        boolean soldOut,
        EventOccurrenceStatus status
) {
}

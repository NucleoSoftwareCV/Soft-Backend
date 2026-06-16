package com.hean.consigueventas.oonabe.event.dto;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;

import java.time.Instant;

public record EventOccurrenceAdminDTO(
        Long id,
        Long eventId,
        String eventTitle,
        Instant startsAt,
        Instant endsAt,
        Long locationId,
        String locationName,
        Integer capacity,
        Integer reservedSpots,
        Integer availableSpots,
        boolean soldOut,
        EventOccurrenceStatus status,
        String virtualUrl
) {
}

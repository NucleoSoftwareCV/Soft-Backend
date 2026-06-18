package com.hean.consigueventas.oonabe.event.dto.response;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;

import java.time.Instant;

public record EventOccurrencePublicResponse(
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

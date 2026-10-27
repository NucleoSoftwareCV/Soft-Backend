package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.event.entity.Event;

import java.time.Instant;

public record RecommendedEvent(
        Event event,
        Long occurrenceId,
        Instant startsAt,
        Instant endsAt,
        String cityName,
        int score,
        String reason
) {
}
package com.hean.consigueventas.oonabe.recommendation.dto.response;

import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;

public record RecommendedEventResponse(
        EventCardResponse event,
        Long occurrenceId,
        int score,
        String reason
) {
}
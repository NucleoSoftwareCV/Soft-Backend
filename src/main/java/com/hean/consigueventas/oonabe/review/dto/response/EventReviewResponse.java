package com.hean.consigueventas.oonabe.review.dto.response;

import java.time.Instant;

public record EventReviewResponse(
        Long id,
        Long eventId,
        String authorName,
        Integer stars,
        String comment,
        Instant createdAt
) {}

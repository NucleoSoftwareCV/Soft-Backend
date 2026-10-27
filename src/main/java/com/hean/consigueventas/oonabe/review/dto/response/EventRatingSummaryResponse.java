package com.hean.consigueventas.oonabe.review.dto.response;

import java.util.List;

public record EventRatingSummaryResponse(Long eventId, Double averageRating, long reviewCount,
                                         List<EventReviewerPreviewResponse> reviewers) {
    public static EventRatingSummaryResponse empty(Long eventId) {
        return new EventRatingSummaryResponse(eventId, null, 0L, List.of());
    }
}

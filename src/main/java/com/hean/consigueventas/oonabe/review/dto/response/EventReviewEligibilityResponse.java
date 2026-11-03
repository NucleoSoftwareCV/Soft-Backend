package com.hean.consigueventas.oonabe.review.dto.response;

public record EventReviewEligibilityResponse(Long eventId, boolean canReview, String reason) {}

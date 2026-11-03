package com.hean.consigueventas.oonabe.review.controller;

import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.common.security.SecurityUtils;
import com.hean.consigueventas.oonabe.review.dto.request.CreateEventReviewRequest;
import com.hean.consigueventas.oonabe.review.dto.response.*;
import com.hean.consigueventas.oonabe.review.service.EventReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/reviews")
@Validated
public class EventReviewController {
    private final EventReviewService reviewService;

    public EventReviewController(EventReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/summary")
    public ResponseEntity<EventRatingSummaryResponse> summary(@PathVariable @Positive Long eventId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(reviewService.getSummary(eventId));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventReviewResponse>> list(
            @PathVariable @Positive Long eventId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(PagedResponse.from(reviewService.getReviews(eventId, page, size)));
    }

    @GetMapping("/eligibility")
    public ResponseEntity<EventReviewEligibilityResponse> eligibility(@PathVariable @Positive Long eventId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
                reviewService.getEligibility(eventId, SecurityUtils.getAuthenticatedUserId()));
    }

    @PostMapping
    public ResponseEntity<EventReviewResponse> create(@PathVariable @Positive Long eventId,
                                                      @Valid @RequestBody CreateEventReviewRequest request) {
        EventReviewResponse review = reviewService.createReview(
                eventId, SecurityUtils.getAuthenticatedUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore()).body(review);
    }
}

package com.hean.consigueventas.oonabe.event.itinerary.controller;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.event.itinerary.dto.EventItineraryItemRequest;
import com.hean.consigueventas.oonabe.event.itinerary.dto.EventItineraryItemResponse;
import com.hean.consigueventas.oonabe.event.itinerary.service.EventItineraryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/itinerary")
public class EventItineraryController {
    private final EventItineraryService itineraryService;

    public EventItineraryController(EventItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @GetMapping
    public List<EventItineraryItemResponse> getItinerary(
            @PathVariable Long eventId) {
        return itineraryService.getItinerary(eventId);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public List<EventItineraryItemResponse> replaceItinerary(
            @PathVariable Long eventId,
            @Valid @RequestBody List<@Valid EventItineraryItemRequest> requests,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        boolean admin = principal.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return itineraryService.replaceItinerary(
            eventId, requests, principal.getId(), admin);
    }
}


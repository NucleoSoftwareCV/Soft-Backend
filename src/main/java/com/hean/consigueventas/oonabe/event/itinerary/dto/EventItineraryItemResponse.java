package com.hean.consigueventas.oonabe.event.itinerary.dto;

import java.time.LocalTime;

public record EventItineraryItemResponse(
        Long id,
        LocalTime startTime,
        LocalTime endTime,
        String title,
        String description,
        Integer sortOrder
) {}

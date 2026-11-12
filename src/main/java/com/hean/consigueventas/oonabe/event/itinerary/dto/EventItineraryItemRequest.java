package com.hean.consigueventas.oonabe.event.itinerary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record EventItineraryItemRequest(
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotBlank @Size(max = 180) String title,
        @Size(max = 1000) String description
) {}

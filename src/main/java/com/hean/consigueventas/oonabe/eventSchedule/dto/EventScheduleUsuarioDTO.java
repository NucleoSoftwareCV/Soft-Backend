package com.hean.consigueventas.oonabe.eventSchedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventScheduleUsuarioDTO(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Boolean booked,
        Integer maxCapacity,
        Integer availableSpots
) {
}
package com.hean.consigueventas.oonabe.eventSchedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;


public record EventScheduleAdminDTO
        (Long id,
         LocalDate date,
         LocalTime startTime,
         LocalTime endTime,
         boolean booked,
         Integer maxCapacity,
         Integer availableSpots) {
}


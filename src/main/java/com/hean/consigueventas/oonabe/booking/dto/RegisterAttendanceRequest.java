package com.hean.consigueventas.oonabe.booking.dto;

import com.hean.consigueventas.oonabe.common.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record RegisterAttendanceRequest(
        @NotNull(message = "El eventAttendeeId es obligatorio")
        Long eventAttendeeId,
        @NotNull(message = "El estado es obligatorio")
        AttendanceStatus status
) {
}

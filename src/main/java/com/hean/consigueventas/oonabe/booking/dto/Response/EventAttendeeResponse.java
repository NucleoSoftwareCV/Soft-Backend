package com.hean.consigueventas.oonabe.booking.dto.Response;

import com.hean.consigueventas.oonabe.common.enums.AttendanceStatus;

import java.time.Instant;

public record EventAttendeeResponse(
        Long id,
        String attendeeEmail,
        String attendeeName,
        String attendeePhone,
        AttendanceStatus attendanceStatus,
        Instant registeredAt

) {
}

package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.common.enums.AttendanceStatus;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;

import java.time.Instant;

public record EventOccurrenceAttendeeDto(
        Long attendeeId,
        String attendeeName,
        String attendeeEmail,
        String attendeePhone,
        AttendanceStatus attendanceStatus,
        // Booking info
        String bookingCode,
        BookingStatus bookingStatus,
        Instant bookingCreatedAt,
        // Buyer / holder info (who paid)
        String buyerFullName,
        String buyerEmail,
        String buyerPhone
) {}

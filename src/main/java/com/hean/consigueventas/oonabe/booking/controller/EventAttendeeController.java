package com.hean.consigueventas.oonabe.booking.controller;

import com.hean.consigueventas.oonabe.booking.dto.EventAttendeeRequest;
import com.hean.consigueventas.oonabe.booking.dto.RegisterAttendanceRequest;
import com.hean.consigueventas.oonabe.booking.dto.Response.EventAttendeeResponse;
import com.hean.consigueventas.oonabe.booking.service.EventAttendeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings/attendance")
public class EventAttendeeController {

    private final EventAttendeeService eventAttendeeService;

    @PostMapping
    public ResponseEntity<List<EventAttendeeResponse>> getEventAttendee(@RequestBody EventAttendeeRequest request) {
        return ResponseEntity.ok(eventAttendeeService.getEventAttendeeByEventBookingId(request.eventId()));
    }

    @PatchMapping
    public ResponseEntity<EventAttendeeResponse> registerAttendance(@RequestBody RegisterAttendanceRequest request) {
        return ResponseEntity.ok(eventAttendeeService.updateAttendanceStatus(request));
    }
}

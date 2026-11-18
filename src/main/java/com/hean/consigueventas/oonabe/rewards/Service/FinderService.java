package com.hean.consigueventas.oonabe.rewards.Service;

import com.hean.consigueventas.oonabe.booking.dto.Response.EventIdAndUserIdResponse;
import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.repository.EventAttendeeRepository;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FinderService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventBookingRepository eventBookingRepository;
    private final EventAttendeeRepository eventAttendeeRepository;

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    public EventAttendee findEventAttendeeById(Long eventAttendeeId){
        return eventAttendeeRepository.findById(eventAttendeeId)
                .orElseThrow(() -> new IllegalArgumentException("EventAttendee not found"));
    }

    public EventIdAndUserIdResponse findEventIdAndUserId(Long attendeeId){
        return eventAttendeeRepository.findEventAndUserByAttendeeId(attendeeId)
                .orElseThrow(() -> new IllegalArgumentException("EventIdAndUserId not found"));
    }
}

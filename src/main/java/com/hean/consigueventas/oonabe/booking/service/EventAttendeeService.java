package com.hean.consigueventas.oonabe.booking.service;

import com.hean.consigueventas.oonabe.booking.Mapper.EventAttendeeMapper;
import com.hean.consigueventas.oonabe.booking.dto.RegisterAttendanceRequest;
import com.hean.consigueventas.oonabe.booking.dto.Response.EventAttendeeResponse;
import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import com.hean.consigueventas.oonabe.booking.repository.EventAttendeeRepository;
import com.hean.consigueventas.oonabe.common.enums.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventAttendeeService {

    private final EventAttendeeRepository eventAttendeeRepository;
    private final EventAttendeeMapper eventAttendeeMapper;

    public List<EventAttendeeResponse> getEventAttendeeByEventBookingId(Long eventId){
        List<EventAttendee> eventAttendees = eventAttendeeRepository.findByEventId(eventId);
        return eventAttendeeMapper.toResponse(eventAttendees);
    }

    public EventAttendeeResponse updateAttendanceStatus(RegisterAttendanceRequest request){
        Long evtAttendeeId = request.eventAttendeeId();
        AttendanceStatus statusRequest = request.status();

        EventAttendee eventAttendee = eventAttendeeRepository.findById(evtAttendeeId)
                .orElseThrow();
        AttendanceStatus status = eventAttendee.getAttendanceStatus();

        if (status == AttendanceStatus.ASISTIO || status == AttendanceStatus.NO_ASISTIO){
            return eventAttendeeMapper.toResponse(eventAttendee);
        }
        if (status == AttendanceStatus.PENDIENTE){
            eventAttendee.setAttendanceStatus(statusRequest);
        }

        EventAttendee newEventAttendee = eventAttendeeRepository.save(eventAttendee);
        return eventAttendeeMapper.toResponse(newEventAttendee);
    }
}

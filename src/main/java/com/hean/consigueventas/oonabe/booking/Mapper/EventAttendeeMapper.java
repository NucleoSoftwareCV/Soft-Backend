package com.hean.consigueventas.oonabe.booking.Mapper;

import com.hean.consigueventas.oonabe.booking.dto.Response.EventAttendeeResponse;
import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventAttendeeMapper {

    EventAttendeeResponse toResponse(EventAttendee transaction);
    List<EventAttendeeResponse> toResponse(List<EventAttendee> transactions);
}

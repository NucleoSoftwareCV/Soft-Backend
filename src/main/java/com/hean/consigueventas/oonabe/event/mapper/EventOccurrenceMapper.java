package com.hean.consigueventas.oonabe.event.mapper;

import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrencePublicResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceResponse;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventOccurrenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "meetingLink", ignore = true)
    @Mapping(target = "reservedSpots", constant = "0")
    @Mapping(target = "status", constant = "PROGRAMADA")
    EventOccurrence toEntity(
            EventOccurrenceRequest request
    );


    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "availableSpots", expression = "java(availableSpots(occurrence))")
    @Mapping(target = "soldOut", expression = "java(isSoldOut(occurrence))")
    EventOccurrenceAdminResponse toAdminDto(EventOccurrence occurrence);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "availableSpots", expression = "java(availableSpots(occurrence))")
    @Mapping(target = "soldOut", expression = "java(isSoldOut(occurrence))")
    EventOccurrencePublicResponse toPublicDto(EventOccurrence occurrence);


    @Mapping(target = "availableSpots", expression = "java(occurrence.getCapacity() - occurrence.getReservedSpots())")
    @Mapping(target = "soldOut", expression = "java(occurrence.getCapacity() - occurrence.getReservedSpots() == 0)")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "meetingLink", source = "meetingLink")
    EventOccurrenceResponse toResponse(EventOccurrence occurrence);


    default Integer availableSpots(EventOccurrence occurrence) {
        return occurrence.getCapacity() - occurrence.getReservedSpots();
    }

    default boolean isSoldOut(EventOccurrence occurrence) {
        return availableSpots(occurrence) == 0;
    }
}

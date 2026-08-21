package com.hean.consigueventas.oonabe.event.mapper;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrencePublicResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceResponse;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface EventOccurrenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
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
    @Mapping(target = "virtualUrl", source = "meetingLink.meetingUrl")
    @Mapping(target = "availableSpots", expression = "java(availableSpots(occurrence))")
    @Mapping(target = "soldOut", expression = "java(isSoldOut(occurrence))")
    @Mapping(target = "status", expression = "java(effectiveStatus(occurrence))")
    EventOccurrenceAdminResponse toAdminDto(EventOccurrence occurrence);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "availableSpots", expression = "java(availableSpots(occurrence))")
    @Mapping(target = "soldOut", expression = "java(isSoldOut(occurrence))")
    @Mapping(target = "status", expression = "java(effectiveStatus(occurrence))")
    EventOccurrencePublicResponse toPublicDto(EventOccurrence occurrence);


    @Mapping(target = "availableSpots", expression = "java(occurrence.getCapacity() - occurrence.getReservedSpots())")
    @Mapping(target = "soldOut", expression = "java(occurrence.getCapacity() - occurrence.getReservedSpots() == 0)")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "location.cityName", source = "location.city.name")
    @Mapping(target = "meetingLink", source = "meetingLink")
    @Mapping(target = "status", expression = "java(effectiveStatus(occurrence).name())")
    EventOccurrenceResponse toResponse(EventOccurrence occurrence);


    default Integer availableSpots(EventOccurrence occurrence) {
        return occurrence.getCapacity() - occurrence.getReservedSpots();
    }

    default boolean isSoldOut(EventOccurrence occurrence) {
        return availableSpots(occurrence) == 0;
    }

    /**
     * Derives a time-aware status for display without mutating the persisted value:
     * a PROGRAMADA occurrence automatically reads as EN_CURSO/FINALIZADA once its
     * window starts/ends, while CANCELADA/AGOTADA/FINALIZADA stay authoritative.
     */
    default EventOccurrenceStatus effectiveStatus(EventOccurrence occurrence) {
        EventOccurrenceStatus stored = occurrence.getStatus();
        if (stored == EventOccurrenceStatus.CANCELADA || stored == EventOccurrenceStatus.FINALIZADA) {
            return stored;
        }
        Instant now = Instant.now();
        if (occurrence.getEndsAt() != null && !now.isBefore(occurrence.getEndsAt())) {
            return EventOccurrenceStatus.FINALIZADA;
        }
        if (occurrence.getStartsAt() != null && !now.isBefore(occurrence.getStartsAt())) {
            return EventOccurrenceStatus.EN_CURSO;
        }
        return stored;
    }
}

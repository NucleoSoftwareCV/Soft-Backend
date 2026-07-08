package com.hean.consigueventas.oonabe.event.mapper;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.event.dto.request.EventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventDetailResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOrganizerResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventResponse;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;

@Mapper(componentModel = "spring", uses = {EventOccurrenceMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "specialist", ignore = true)
    @Mapping(target = "occurrences", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "eventType", ignore = true)
    @Mapping(target = "recurring", ignore = true)
    Event toEntity(EventUpsertRequest request);

    @AfterMapping
    default void ensureDetailCollections(@MappingTarget Event event) {
        if (event.getIncludes() == null) {
            event.setIncludes(new ArrayList<>());
        }
        if (event.getHighlights() == null) {
            event.setHighlights(new ArrayList<>());
        }
        if (event.getWhatToBring() == null) {
            event.setWhatToBring(new ArrayList<>());
        }
    }

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    EventResponse toResponse(Event event);

    @Mapping(target = "organizer", source = "specialist")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "isRecurring", source = "recurring")
    EventDetailResponse toDetailResponse(Event event);

    EventOrganizerResponse toOrganizerResponse(SpecialistProfile specialist);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "organizerId", source = "specialist.id")
    @Mapping(target = "organizerName", source = "specialist.publicName")
    @Mapping(target = "organizerPhotoUrl", source = "specialist.photoUrl")
    @Mapping(target = "startsAt", source = ".", qualifiedByName = "nextStartsAt")
    @Mapping(target = "endsAt", source = ".", qualifiedByName = "nextEndsAt")
    @Mapping(target = "cityName", source = ".", qualifiedByName = "nextCityName")
    @Mapping(target = "isRecurring", source = "recurring")
    EventCardResponse toCardResponse(Event event);

    @Named("nextStartsAt")
    default Instant nextStartsAt(Event event) {
        EventOccurrence occurrence = nextOccurrence(event);
        return occurrence == null ? null : occurrence.getStartsAt();
    }

    @Named("nextEndsAt")
    default Instant nextEndsAt(Event event) {
        EventOccurrence occurrence = nextOccurrence(event);
        return occurrence == null ? null : occurrence.getEndsAt();
    }

    @Named("nextCityName")
    default String nextCityName(Event event) {
        EventOccurrence occurrence = nextOccurrence(event);
        if (occurrence == null || occurrence.getLocation() == null || occurrence.getLocation().getCity() == null) {
            return null;
        }
        return occurrence.getLocation().getCity().getName();
    }

    default EventOccurrence nextOccurrence(Event event) {
        if (event == null || event.getOccurrences() == null || event.getOccurrences().isEmpty()) {
            return null;
        }

        return event.getOccurrences().stream()
                .filter(occurrence -> occurrence.getStatus() == EventOccurrenceStatus.PROGRAMADA)
                .min(Comparator.comparing(EventOccurrence::getStartsAt))
                .orElse(event.getOccurrences().getFirst());
    }

}

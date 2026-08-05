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
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = {EventOccurrenceMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "specialist", ignore = true)
    @Mapping(target = "occurrences", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "eventType", ignore = true)
    @Mapping(target = "recurring", ignore = true)
    void updateEntity(EventUpsertRequest request, @MappingTarget Event event);

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
        if (event.getPaymentMethod() == null) {
            event.setPaymentMethod(com.hean.consigueventas.oonabe.common.enums.EventPaymentMethod.WHATSAPP);
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

    @Mapping(target = "categoryId", source = "event.category.id")
    @Mapping(target = "categoryName", source = "event.category.name")
    @Mapping(target = "organizerId", source = "event.specialist.id")
    @Mapping(target = "organizerName", source = "event.specialist.publicName")
    @Mapping(target = "organizerPhotoUrl", source = "event.specialist.photoUrl")
    @Mapping(target = "coverImageUrl", source = "coverImageUrl")
    @Mapping(target = "startsAt", source = "occurrences", qualifiedByName = "nextStartsAt")
    @Mapping(target = "endsAt", source = "occurrences", qualifiedByName = "nextEndsAt")
    @Mapping(target = "cityName", source = "occurrences", qualifiedByName = "nextCityName")
    @Mapping(target = "isRecurring", source = "event.recurring")
    EventCardResponse toCardResponse(
            Event event,
            String coverImageUrl,
            List<EventOccurrence> occurrences,
            @Context Instant notBefore);

    @Named("nextStartsAt")
    default Instant nextStartsAt(List<EventOccurrence> occurrences, @Context Instant notBefore) {
        EventOccurrence occurrence = nextOccurrence(occurrences, notBefore);
        return occurrence == null ? null : occurrence.getStartsAt();
    }

    @Named("nextEndsAt")
    default Instant nextEndsAt(List<EventOccurrence> occurrences, @Context Instant notBefore) {
        EventOccurrence occurrence = nextOccurrence(occurrences, notBefore);
        return occurrence == null ? null : occurrence.getEndsAt();
    }

    @Named("nextCityName")
    default String nextCityName(List<EventOccurrence> occurrences, @Context Instant notBefore) {
        EventOccurrence occurrence = nextOccurrence(occurrences, notBefore);
        if (occurrence == null || occurrence.getLocation() == null || occurrence.getLocation().getCity() == null) {
            return null;
        }
        return occurrence.getLocation().getCity().getName();
    }

    default EventOccurrence nextOccurrence(List<EventOccurrence> occurrences, Instant notBefore) {
        if (occurrences == null || occurrences.isEmpty()) {
            return null;
        }

        return occurrences.stream()
                .filter(occurrence -> occurrence.getStatus() == EventOccurrenceStatus.PROGRAMADA)
                .filter(occurrence -> notBefore == null || !occurrence.getStartsAt().isBefore(notBefore))
                .min(Comparator.comparing(EventOccurrence::getStartsAt))
                .orElse(null);
    }

}

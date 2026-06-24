package com.hean.consigueventas.oonabe.event.mapper;

import com.hean.consigueventas.oonabe.event.dto.request.EventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventDetailResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOrganizerResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EventOccurrenceMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "specialist", ignore = true)
    @Mapping(target = "occurrences", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventUpsertRequest request);


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

    default EventCardResponse toCardResponse(Event event) {
        if (event == null) {
            return null;
        }

        Long id = event.getId();
        String title = event.getTitle();
        String summary = event.getSummary();
        com.hean.consigueventas.oonabe.common.enums.EventModality modality = event.getModality();
        java.math.BigDecimal priceFrom = event.getPriceFrom();
        String currency = event.getCurrency();

        Long categoryId = null;
        String categoryName = null;
        if (event.getCategory() != null) {
            categoryId = event.getCategory().getId();
            categoryName = event.getCategory().getName();
        }

        Long organizerId = null;
        String organizerName = null;
        String organizerPhotoUrl = null;
        if (event.getSpecialist() != null) {
            organizerId = event.getSpecialist().getId();
            organizerName = event.getSpecialist().getPublicName();
            organizerPhotoUrl = event.getSpecialist().getPhotoUrl();
        }

        java.time.Instant startsAt = null;
        java.time.Instant endsAt = null;
        String cityName = null;

        if (event.getOccurrences() != null && !event.getOccurrences().isEmpty()) {
            var firstOccurrence = event.getOccurrences().stream()
                    .filter(o -> o.getStatus() == com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus.PROGRAMADA)
                    .min(java.util.Comparator.comparing(com.hean.consigueventas.oonabe.event.entity.EventOccurrence::getStartsAt))
                    .orElse(event.getOccurrences().get(0));

            startsAt = firstOccurrence.getStartsAt();
            endsAt = firstOccurrence.getEndsAt();
            if (firstOccurrence.getLocation() != null && firstOccurrence.getLocation().getCity() != null) {
                cityName = firstOccurrence.getLocation().getCity().getName();
            }
        }

        return new EventCardResponse(
                id, title, summary, modality, priceFrom, currency,
                categoryId, categoryName, organizerId, organizerName, organizerPhotoUrl,
                startsAt, endsAt, cityName,
                event.getEventType(),
                event.isRecurring()
        );
    }

}

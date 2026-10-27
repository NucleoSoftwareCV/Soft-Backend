package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.config.TimeConfig;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Objects;

public final class RecommendationEventFilter {

    public boolean matches(
            EventOccurrence occurrence,
            EventFilterRequest filter
    ) {
        Objects.requireNonNull(occurrence, "La fecha del evento es obligatoria");

        if (filter == null) {
            return true;
        }

        Event event = occurrence.getEvent();

        return matchesSearch(event, filter.search())
                && matchesCategory(event, filter)
                && matchesExperienceType(event, filter)
                && matchesModality(event, filter)
                && (filter.mood() == null || filter.mood() == event.getMood())
                && matchesSpecialist(event, filter)
                && matchesRecurrence(event, filter)
                && matchesPrice(event, filter)
                && matchesCity(occurrence, filter)
                && matchesDateAndHour(occurrence, filter);
    }

    private boolean matchesSearch(Event event, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String term = normalize(search);

        return normalize(event.getTitle()).contains(term)
                || normalize(event.getSummary()).contains(term);
    }

    private boolean matchesCategory(Event event, EventFilterRequest filter) {
        if (filter.categoryId() != null) {
            return event.getCategory() != null
                    && filter.categoryId().equals(event.getCategory().getId());
        }

        if (filter.categoryIds() != null && !filter.categoryIds().isEmpty()) {
            return event.getCategory() != null
                    && filter.categoryIds().contains(event.getCategory().getId());
        }

        return true;
    }

    private boolean matchesExperienceType(
            Event event,
            EventFilterRequest filter
    ) {
        return filter.experienceTypeId() == null
                || (
                event.getExperienceType() != null
                        && filter.experienceTypeId()
                        .equals(event.getExperienceType().getId())
        );
    }

    private boolean matchesModality(Event event, EventFilterRequest filter) {
        return filter.modality() == null
                || filter.modality() == event.getModality();
    }

    private boolean matchesSpecialist(Event event, EventFilterRequest filter) {
        return filter.specialistId() == null
                || (
                event.getSpecialist() != null
                        && filter.specialistId()
                        .equals(event.getSpecialist().getId())
        );
    }

    private boolean matchesRecurrence(Event event, EventFilterRequest filter) {
        return filter.isRecurring() == null
                || filter.isRecurring() == event.isRecurring();
    }

    private boolean matchesPrice(Event event, EventFilterRequest filter) {
        if (filter.minPrice() == null && filter.maxPrice() == null) {
            return true;
        }

        BigDecimal price = event.getPriceFrom();

        if (price == null) {
            return false;
        }

        return (filter.minPrice() == null
                || price.compareTo(filter.minPrice()) >= 0)
                && (filter.maxPrice() == null
                || price.compareTo(filter.maxPrice()) <= 0);
    }

    private boolean matchesCity(
            EventOccurrence occurrence,
            EventFilterRequest filter
    ) {
        if (filter.cityName() == null || filter.cityName().isBlank()) {
            return true;
        }

        if (occurrence.getEvent().getModality() == EventModality.ONLINE) {
            return Boolean.TRUE.equals(filter.includeOnline());
        }

        if (occurrence.getLocation() == null
                || occurrence.getLocation().getCity() == null) {
            return false;
        }

        return normalize(filter.cityName()).equals(
                normalize(occurrence.getLocation().getCity().getName())
        );
    }

    private boolean matchesDateAndHour(
            EventOccurrence occurrence,
            EventFilterRequest filter
    ) {
        ZonedDateTime start = occurrence.getStartsAt()
                .atZone(TimeConfig.BUSINESS_ZONE);

        if (filter.dateFrom() != null
                && start.toLocalDate().isBefore(filter.dateFrom())) {
            return false;
        }

        if (filter.dateTo() != null
                && start.toLocalDate().isAfter(filter.dateTo())) {
            return false;
        }

        if (filter.hourFrom() != null
                && start.getHour() < filter.hourFrom()) {
            return false;
        }

        return filter.hourTo() == null
                || start.getHour() <= filter.hourTo();
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.strip().toLowerCase(Locale.ROOT);
    }
}
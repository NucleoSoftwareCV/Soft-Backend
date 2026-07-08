package com.hean.consigueventas.oonabe.event.specification;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class EventSpecification {

    private EventSpecification() {
    }

    public static Specification<Event> publicListing(EventFilterRequest filter, Sort.Direction startsAtDirection) {
        Specification<Event> spec = isPublished();

        if (hasText(filter.search())) {
            spec = spec.and(titleOrSummaryContains(filter.search()));
        }
        if (filter.categoryId() != null) {
            spec = spec.and(hasCategory(filter.categoryId()));
        }
        if (filter.eventType() != null) {
            spec = spec.and(hasEventType(filter.eventType()));
        }
        if (filter.modality() != null) {
            spec = spec.and(hasModality(filter.modality()));
        }
        if (hasText(filter.cityName())) {
            spec = spec.and(hasCity(filter.cityName()));
        }
        if (filter.minPrice() != null) {
            spec = spec.and(minPrice(filter.minPrice()));
        }
        if (filter.maxPrice() != null) {
            spec = spec.and(maxPrice(filter.maxPrice()));
        }
        if (filter.dateFrom() != null || filter.dateTo() != null) {
            spec = spec.and(occurrenceInDateRange(filter.dateFrom(), filter.dateTo()));
        }
        if (filter.hourFrom() != null && filter.hourTo() != null) {
            spec = spec.and(occurrenceInHourRange(filter.hourFrom(), filter.hourTo()));
        }
        if (filter.isRecurring() != null) {
            spec = spec.and(isRecurring(filter.isRecurring()));
        }
        if (startsAtDirection != null) {
            spec = spec.and(orderByNextProgrammedOccurrence(startsAtDirection));
        }

        return spec;
    }

    public static Specification<Event> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("status"), EventStatus.PUBLICADO);
    }

    public static Specification<Event> titleOrSummaryContains(String text) {
        return (root, query, cb) -> {
            String pattern = "%" + text.toLowerCase() + "%";
            Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
            Predicate summaryMatch = cb.like(cb.lower(root.get("summary")), pattern);
            return cb.or(titleMatch, summaryMatch);
        };
    }

    public static Specification<Event> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Event> hasSpecialist(Long specialistId) {
        return (root, query, cb) -> cb.equal(root.get("specialist").get("id"), specialistId);
    }

    public static Specification<Event> hasDifferentSpecialist(Long specialistId) {
        return (root, query, cb) -> cb.notEqual(root.get("specialist").get("id"), specialistId);
    }

    public static Specification<Event> excludeEvent(Long eventId) {
        return (root, query, cb) -> cb.notEqual(root.get("id"), eventId);
    }

    public static Specification<Event> hasEventType(EventType eventType) {
        return (root, query, cb) -> cb.equal(root.get("eventType"), eventType);
    }

    public static Specification<Event> hasModality(EventModality modality) {
        return (root, query, cb) -> cb.equal(root.get("modality"), modality);
    }

    public static Specification<Event> hasCity(String cityName) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<EventOccurrence> occurrence = subquery.from(EventOccurrence.class);
            Join<EventOccurrence, Location> location = occurrence.join("location", JoinType.INNER);
            Join<Location, City> city = location.join("city", JoinType.INNER);

            subquery.select(occurrence.get("id"));
            subquery.where(
                    cb.equal(occurrence.get("event"), root),
                    cb.equal(cb.lower(city.get("name")), cityName.toLowerCase())
            );
            return cb.exists(subquery);
        };
    }

    public static Specification<Event> minPrice(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceFrom"), minPrice);
    }

    public static Specification<Event> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceFrom"), maxPrice);
    }

    public static Specification<Event> occurrenceInDateRange(LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<EventOccurrence> occurrence = subquery.from(EventOccurrence.class);

            Predicate fromPredicate = dateFrom != null
                    ? cb.greaterThanOrEqualTo(occurrence.get("startsAt"), dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC))
                    : cb.conjunction();

            Predicate toPredicate = dateTo != null
                    ? cb.lessThanOrEqualTo(occurrence.get("startsAt"), dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
                    : cb.conjunction();

            subquery.select(occurrence.get("id"));
            subquery.where(
                    cb.equal(occurrence.get("event"), root),
                    cb.equal(occurrence.get("status"), EventOccurrenceStatus.PROGRAMADA),
                    fromPredicate,
                    toPredicate
            );
            return cb.exists(subquery);
        };
    }

    public static Specification<Event> occurrenceInHourRange(int hourFrom, int hourTo) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<EventOccurrence> occurrence = subquery.from(EventOccurrence.class);

            HibernateCriteriaBuilder hibernateCriteriaBuilder = (HibernateCriteriaBuilder) cb;
            var hourExpression = hibernateCriteriaBuilder.hour(occurrence.get("startsAt"));

            subquery.select(occurrence.get("id"));
            subquery.where(
                    cb.equal(occurrence.get("event"), root),
                    cb.equal(occurrence.get("status"), EventOccurrenceStatus.PROGRAMADA),
                    cb.between(hourExpression, hourFrom, hourTo)
            );
            return cb.exists(subquery);
        };
    }

    public static Specification<Event> isRecurring(boolean recurring) {
        return (root, query, cb) -> cb.equal(root.get("isRecurring"), recurring);
    }

    public static Specification<Event> orderByNextProgrammedOccurrence(Sort.Direction direction) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                Subquery<Instant> nextStart = query.subquery(Instant.class);
                Root<EventOccurrence> occurrence = nextStart.from(EventOccurrence.class);
                nextStart.select(cb.least(occurrence.<Instant>get("startsAt")));
                nextStart.where(
                        cb.equal(occurrence.get("event"), root),
                        cb.equal(occurrence.get("status"), EventOccurrenceStatus.PROGRAMADA)
                );
                query.orderBy(direction.isAscending() ? cb.asc(nextStart) : cb.desc(nextStart));
            }
            return cb.conjunction();
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

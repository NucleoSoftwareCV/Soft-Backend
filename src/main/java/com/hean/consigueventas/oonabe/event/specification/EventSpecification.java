package com.hean.consigueventas.oonabe.event.specification;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.config.TimeConfig;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
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
import java.util.Collection;
import java.util.Locale;

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
        } else if (filter.categoryIds() != null && !filter.categoryIds().isEmpty()) {
            spec = spec.and(hasAnyCategory(filter.categoryIds()));
        }
        if (filter.experienceTypeId() != null) {
            spec = spec.and(hasExperienceType(filter.experienceTypeId()));
        }
        if (filter.modality() != null) {
            spec = spec.and(hasModality(filter.modality()));
        }
        if (hasText(filter.cityName())) {
            spec = spec.and(Boolean.TRUE.equals(filter.includeOnline())
                    ? hasCityOrIsOnline(filter.cityName())
                    : hasCity(filter.cityName()));
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
        return (root, query, cb) -> {
            // LEFT JOIN explicito: eventos creados antes del catalogo de tipos de
            // experiencia pueden tener experience_type_id nulo. Un path traversal
            // simple (root.get("experienceType")) genera un INNER JOIN implicito
            // que los excluiria por completo del listado publico.
            Join<Event, ExperienceType> experienceType = root.join("experienceType", JoinType.LEFT);
            return cb.and(
                    cb.equal(root.get("status"), EventStatus.PUBLICADO),
                    cb.isTrue(root.get("category").get("active")),
                    cb.or(
                            cb.isNull(experienceType.get("id")),
                            cb.isTrue(experienceType.get("active"))
                    )
            );
        };
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

    public static Specification<Event> hasAnyCategory(Collection<Long> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
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

    public static Specification<Event> hasExperienceType(Long experienceTypeId) {
        return (root, query, cb) -> cb.equal(root.get("experienceType").get("id"), experienceTypeId);
    }

    public static Specification<Event> hasExperienceTypeSlug(String slug) {
        return (root, query, cb) -> cb.equal(root.get("experienceType").get("slug"), slug);
    }

    public static Specification<Event> hasModality(EventModality modality) {
        return (root, query, cb) -> cb.equal(root.get("modality"), modality);
    }

    public static Specification<Event> hasCity(String cityName) {
        return (root, query, cb) -> {
            return cityExists(root, query.subquery(Long.class), cb, cityName);
        };
    }

    public static Specification<Event> hasCityOrIsOnline(String cityName) {
        return (root, query, cb) -> {
            Predicate inCity = cityExists(root, query.subquery(Long.class), cb, cityName);
            Predicate online = cb.equal(root.get("modality"), EventModality.ONLINE);
            return cb.or(inCity, online);
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
                    ? cb.greaterThanOrEqualTo(occurrence.get("startsAt"),
                            dateFrom.atStartOfDay(TimeConfig.BUSINESS_ZONE).toInstant())
                    : cb.conjunction();

            Predicate toPredicate = dateTo != null
                    ? cb.lessThan(occurrence.get("startsAt"),
                            dateTo.plusDays(1).atStartOfDay(TimeConfig.BUSINESS_ZONE).toInstant())
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

    public static Specification<Event> hasProgrammedOccurrenceBetween(Instant fromInclusive, Instant toExclusive) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<EventOccurrence> occurrence = subquery.from(EventOccurrence.class);
            Predicate toPredicate = toExclusive == null
                    ? cb.conjunction()
                    : cb.lessThan(occurrence.get("startsAt"), toExclusive);

            subquery.select(occurrence.get("id"));
            subquery.where(
                    cb.equal(occurrence.get("event"), root),
                    cb.equal(occurrence.get("status"), EventOccurrenceStatus.PROGRAMADA),
                    cb.greaterThanOrEqualTo(occurrence.get("startsAt"), fromInclusive),
                    toPredicate
            );
            return cb.exists(subquery);
        };
    }

    public static Specification<Event> hasProgrammedOccurrenceInCityBetween(
            String cityName,
            Instant fromInclusive,
            Instant toExclusive) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<EventOccurrence> occurrence = subquery.from(EventOccurrence.class);
            Join<EventOccurrence, Location> location = occurrence.join("location", JoinType.INNER);
            Join<Location, City> city = location.join("city", JoinType.INNER);
            Predicate toPredicate = toExclusive == null
                    ? cb.conjunction()
                    : cb.lessThan(occurrence.get("startsAt"), toExclusive);

            subquery.select(occurrence.get("id"));
            subquery.where(
                    cb.equal(occurrence.get("event"), root),
                    cb.equal(occurrence.get("status"), EventOccurrenceStatus.PROGRAMADA),
                    cb.greaterThanOrEqualTo(occurrence.get("startsAt"), fromInclusive),
                    cb.equal(cb.lower(city.get("name")), cityName.toLowerCase(Locale.ROOT)),
                    toPredicate
            );
            return cb.exists(subquery);
        };
    }

    public static Specification<Event> orderByNextProgrammedOccurrence(Sort.Direction direction) {
        return orderByNextProgrammedOccurrence(direction, null);
    }

    public static Specification<Event> orderByNextProgrammedOccurrence(
            Sort.Direction direction,
            Instant notBefore) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                Subquery<Instant> nextStart = query.subquery(Instant.class);
                Root<EventOccurrence> occurrence = nextStart.from(EventOccurrence.class);
                nextStart.select(cb.least(occurrence.<Instant>get("startsAt")));
                Predicate notBeforePredicate = notBefore == null
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(occurrence.get("startsAt"), notBefore);
                nextStart.where(cb.equal(occurrence.get("event"), root),
                        cb.equal(occurrence.get("status"), EventOccurrenceStatus.PROGRAMADA),
                        notBeforePredicate);
                query.orderBy(direction.isAscending() ? cb.asc(nextStart) : cb.desc(nextStart));
            }
            return cb.conjunction();
        };
    }

    private static Predicate cityExists(
            Root<Event> event,
            Subquery<Long> subquery,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            String cityName) {
        Root<EventOccurrence> occurrence = subquery.from(EventOccurrence.class);
        Join<EventOccurrence, Location> location = occurrence.join("location", JoinType.INNER);
        Join<Location, City> city = location.join("city", JoinType.INNER);

        subquery.select(occurrence.get("id"));
        subquery.where(
                cb.equal(occurrence.get("event"), event),
                cb.equal(cb.lower(city.get("name")), cityName.toLowerCase(Locale.ROOT))
        );
        return cb.exists(subquery);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

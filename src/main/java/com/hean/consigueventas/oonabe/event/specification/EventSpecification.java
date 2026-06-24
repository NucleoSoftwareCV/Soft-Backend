package com.hean.consigueventas.oonabe.event.specification;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Specifications dinámicas para filtrar eventos.
 * Cada método devuelve una Specification<Event> componible con .and() / .or().
 */
public final class EventSpecification {

    private EventSpecification() {
    }

    /** Solo eventos publicados (siempre se aplica). */
    public static Specification<Event> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("status"), EventStatus.PUBLICADO);
    }

    /** Búsqueda de texto en título o resumen (case-insensitive). */
    public static Specification<Event> titleOrSummaryContains(String text) {
        return (root, query, cb) -> {
            String pattern = "%" + text.toLowerCase() + "%";
            Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
            Predicate summaryMatch = cb.like(cb.lower(root.get("summary")), pattern);
            return cb.or(titleMatch, summaryMatch);
        };
    }

    /** Filtro por ID de categoría. */
    public static Specification<Event> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    /** Filtro por tipo de experiencia. */
    public static Specification<Event> hasEventType(EventType eventType) {
        return (root, query, cb) -> cb.equal(root.get("eventType"), eventType);
    }

    /** Filtro por modalidad (ONLINE / PRESENCIAL). */
    public static Specification<Event> hasModality(EventModality modality) {
        return (root, query, cb) -> cb.equal(root.get("modality"), modality);
    }

    /** Filtro por nombre de ciudad (JOIN occurrences → location → city). */
    public static Specification<Event> hasCity(String cityName) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Event, EventOccurrence> occurrences = root.join("occurrences", JoinType.INNER);
            Join<EventOccurrence, Location> location = occurrences.join("location", JoinType.INNER);
            Join<Location, City> city = location.join("city", JoinType.INNER);
            return cb.equal(cb.lower(city.get("name")), cityName.toLowerCase());
        };
    }

    /** Filtro por precio mínimo. Usa 0 para eventos gratuitos. */
    public static Specification<Event> minPrice(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceFrom"), minPrice);
    }

    /** Filtro por precio máximo. */
    public static Specification<Event> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceFrom"), maxPrice);
    }

    /**
     * Filtro por rango de fechas: busca eventos que tengan al menos una ocurrencia
     * programada cuyo startsAt esté dentro del rango [dateFrom, dateTo].
     */
    public static Specification<Event> occurrenceInDateRange(LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Event, EventOccurrence> occ = root.join("occurrences", JoinType.INNER);
            occ.on(cb.equal(occ.get("status"), EventOccurrenceStatus.PROGRAMADA));

            Predicate fromPredicate = dateFrom != null
                    ? cb.greaterThanOrEqualTo(occ.get("startsAt"), dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC))
                    : cb.conjunction();

            Predicate toPredicate = dateTo != null
                    ? cb.lessThanOrEqualTo(occ.get("startsAt"), dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
                    : cb.conjunction();

            return cb.and(fromPredicate, toPredicate);
        };
    }

    /**
     * Filtro por hora del día usando HOUR() de la base de datos.
     * hourFrom y hourTo son valores entre 0 y 23.
     * Trabaja sobre UTC (la misma zona que se almacena en startsAt).
     */
    public static Specification<Event> occurrenceInHourRange(int hourFrom, int hourTo) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Event, EventOccurrence> occ = root.join("occurrences", JoinType.INNER);
            occ.on(cb.equal(occ.get("status"), EventOccurrenceStatus.PROGRAMADA));

            // HOUR() es una función estándar de H2 y MySQL/PostgreSQL
            var hourExpr = cb.function("HOUR", Integer.class, occ.get("startsAt"));
            return cb.between(hourExpr, hourFrom, hourTo);
        };
    }

    /** Filtro por recurrencia: true = recurrente, false = único. */
    public static Specification<Event> isRecurring(boolean recurring) {
        return (root, query, cb) -> cb.equal(root.get("isRecurring"), recurring);
    }
}

package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventOccurrenceRepository extends JpaRepository<EventOccurrence, Long> {

    boolean existsByEventId(Long eventId);

    java.util.Optional<EventOccurrence> findFirstByEventId(Long eventId);

    @EntityGraph(attributePaths = {"event", "event.specialist", "event.specialist.user", "location", "location.city", "meetingLink"})
    @Query("select occurrence from EventOccurrence occurrence where occurrence.id = :id")
    Optional<EventOccurrence> findManagementById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"location", "location.city", "meetingLink"})
    List<EventOccurrence> findByEventIdOrderByStartsAtAsc(Long eventId);

    @EntityGraph(attributePaths = {"location", "location.city"})
    @Query("""
            select occurrence
            from EventOccurrence occurrence
            where occurrence.event.id in :eventIds
              and occurrence.status = com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus.PROGRAMADA
            order by occurrence.event.id, occurrence.startsAt
            """)
    List<EventOccurrence> findProgrammedByEventIds(@Param("eventIds") Collection<Long> eventIds);

    @EntityGraph(attributePaths = {"event"})
    @Query("""
            select occurrence
            from EventOccurrence occurrence
            where occurrence.event.specialist.id = :specialistId
              and occurrence.event.status = com.hean.consigueventas.oonabe.common.enums.EventStatus.PUBLICADO
              and occurrence.status = com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus.PROGRAMADA
              and occurrence.startsAt >= :from
              and occurrence.startsAt < :to
            order by occurrence.startsAt asc
            """)
    List<EventOccurrence> findPublicCalendarOccurrences(
            @Param("specialistId") Long specialistId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}

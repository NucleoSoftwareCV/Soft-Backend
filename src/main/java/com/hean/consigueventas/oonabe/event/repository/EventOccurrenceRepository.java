package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EventOccurrenceRepository extends JpaRepository<EventOccurrence, Long> {

    boolean existsByEventId(Long eventId);

    java.util.Optional<EventOccurrence> findFirstByEventId(Long eventId);

    @EntityGraph(attributePaths = {"location", "location.city"})
    @Query("""
            select occurrence
            from EventOccurrence occurrence
            where occurrence.event.id in :eventIds
              and occurrence.status = com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus.PROGRAMADA
            order by occurrence.event.id, occurrence.startsAt
            """)
    List<EventOccurrence> findProgrammedByEventIds(@Param("eventIds") Collection<Long> eventIds);
}

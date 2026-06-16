package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EventOccurrenceRepository extends JpaRepository<EventOccurrence, Long> {

    List<EventOccurrence> findByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
            Instant startsAtFrom,
            Instant startsAtTo
    );

    List<EventOccurrence> findByEventIdOrderByStartsAtAsc(Long eventId);

    List<EventOccurrence> findByEventIdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
            Long eventId,
            Instant startsAtFrom,
            Instant startsAtTo
    );
}

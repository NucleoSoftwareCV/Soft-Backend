package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventOccurrenceRepository extends JpaRepository<EventOccurrence, Long> {

    boolean existsByEventId(Long eventId);
}

package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}

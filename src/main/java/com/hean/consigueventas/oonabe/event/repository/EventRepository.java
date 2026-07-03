package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {
    Optional<Event> findByTitle(String title);
}


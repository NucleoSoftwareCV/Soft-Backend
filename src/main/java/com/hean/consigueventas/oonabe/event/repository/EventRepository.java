package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {
    Optional<Event> findByTitle(String title);

    @EntityGraph(attributePaths = {
            "category",
            "specialist",
            "occurrences",
            "occurrences.location",
            "occurrences.location.city",
            "occurrences.meetingLink",
            "includes",
            "highlights",
            "whatToBring"
    })
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findDetailById(@Param("id") Long id);
}


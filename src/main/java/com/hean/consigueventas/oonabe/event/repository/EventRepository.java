package com.hean.consigueventas.oonabe.event.repository;

import com.hean.consigueventas.oonabe.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {
    Optional<Event> findByTitle(String title);

    @Override
    @EntityGraph(attributePaths = {
            "category",
            "specialist"
    })
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);

    @EntityGraph(attributePaths = {
            "category",
            "specialist",
            "occurrences",
            "occurrences.location",
            "occurrences.location.city",
            "occurrences.meetingLink"
    })
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findDetailById(@Param("id") Long id);
}


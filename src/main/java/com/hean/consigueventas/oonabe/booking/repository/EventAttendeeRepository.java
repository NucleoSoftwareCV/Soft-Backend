package com.hean.consigueventas.oonabe.booking.repository;

import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {
    List<EventAttendee> findByEventBookingId(Long eventBookingId);

    @Query("""
        SELECT a FROM EventAttendee a
        JOIN FETCH a.eventBooking b
        JOIN FETCH b.customer c
        JOIN FETCH c.user
        WHERE b.occurrence.id = :occurrenceId
    """)
    List<EventAttendee> findByOccurrenceId(@Param("occurrenceId") Long occurrenceId);
}

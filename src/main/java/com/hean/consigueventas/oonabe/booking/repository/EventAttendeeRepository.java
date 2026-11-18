package com.hean.consigueventas.oonabe.booking.repository;

import com.hean.consigueventas.oonabe.booking.dto.Response.EventIdAndUserIdResponse;
import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Query("""
        SELECT ea FROM EventAttendee ea
        JOIN ea.eventBooking eb
        JOIN eb.occurrence eo
        JOIN eo.event e
        WHERE e.id = :eventId
    """)
    List<EventAttendee> findByEventId(@Param("eventId") Long eventId);

    @Query(value = """
        SELECT ea.id, e.id, u.id
        FROM event_attendees ea
        INNER JOIN event_bookings eb
            ON eb.id = ea.event_booking_id
        INNER JOIN event_occurrences eo
            ON eo.id = eb.occurrence_id
        INNER JOIN events e
            ON e.id = eo.event_id
        INNER JOIN "users" u
            ON u.id = ea.registered_by
        WHERE ea.id = :attendeeId
        """, nativeQuery = true)
    Optional<EventIdAndUserIdResponse> findEventAndUserByAttendeeId(
            @Param("attendeeId") Long attendeeId
    );
}

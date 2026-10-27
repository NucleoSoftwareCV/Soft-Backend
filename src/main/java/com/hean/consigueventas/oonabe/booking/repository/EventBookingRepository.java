package com.hean.consigueventas.oonabe.booking.repository;

import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.Instant;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {
    Optional<EventBooking> findByCode(String code);

    List<EventBooking> findByCustomer_User_IdOrderByCreatedAtDesc(Long userId);
    @Query("""
        select (count(b) > 0) from EventBooking b
        where b.occurrence.event.id = :eventId
          and b.customer.user.id = :userId
          and b.status in :statuses
          and b.occurrence.status <> :cancelled
          and b.occurrence.endsAt <= :now
        """)
    boolean hasCompletedEventReservation(
            @Param("eventId") Long eventId,
            @Param("userId") Long userId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("cancelled") EventOccurrenceStatus cancelled,
            @Param("now") Instant now);
}

package com.hean.consigueventas.oonabe.booking.repository;

import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {
    Optional<EventBooking> findByCode(String code);
}

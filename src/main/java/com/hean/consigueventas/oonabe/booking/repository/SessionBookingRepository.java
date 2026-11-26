package com.hean.consigueventas.oonabe.booking.repository;

import com.hean.consigueventas.oonabe.booking.entity.SessionBooking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionBookingRepository extends JpaRepository<SessionBooking, Long> {

    /** Citas 1:1 que se pagan con la orden; se bloquean para que el pago y la cancelación no se crucen. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from SessionBooking b where b.orderItem.order.id = :orderId order by b.id")
    List<SessionBooking> findLockedByOrderId(@Param("orderId") Long orderId);
}

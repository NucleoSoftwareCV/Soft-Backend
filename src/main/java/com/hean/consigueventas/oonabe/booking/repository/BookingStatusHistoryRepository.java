package com.hean.consigueventas.oonabe.booking.repository;

import com.hean.consigueventas.oonabe.booking.entity.BookingStatusHistory;
import com.hean.consigueventas.oonabe.common.enums.PurchaseItemType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {
    @EntityGraph(attributePaths = "changedBy")
    List<BookingStatusHistory> findByBookingTypeAndBookingIdOrderByCreatedAtAscIdAsc(
            PurchaseItemType bookingType, Long bookingId);
}

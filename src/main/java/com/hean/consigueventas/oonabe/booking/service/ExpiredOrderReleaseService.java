package com.hean.consigueventas.oonabe.booking.service;

import com.hean.consigueventas.oonabe.booking.entity.BookingStatusHistory;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.repository.BookingStatusHistoryRepository;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.enums.PurchaseItemType;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Releases the spots held by orders whose payment window expired. Bookings an expired order holds
 * (pending or accepted but unpaid) can never be paid, so they are cancelled and their spots freed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpiredOrderReleaseService {
    static final String REASON = "Orden expirada sin pago";
    private static final Set<BookingStatus> HELD = Set.of(BookingStatus.PENDIENTE, BookingStatus.CONFIRMADA);

    private final PurchaseOrderRepository orders;
    private final EventBookingRepository bookings;
    private final EventOccurrenceRepository occurrences;
    private final BookingStatusHistoryRepository history;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<String> findReleasableOrderCodes() {
        return bookings.findOrderCodesWithExpiredHolds(clock.instant(), PurchaseOrderStatus.PAGADA, HELD);
    }

    /** Returns how many bookings were released; 0 when the order was paid or released meanwhile. */
    @Transactional
    public int release(String orderCode) {
        // Same locking order as payment: order, booking(s), occurrence(s).
        PurchaseOrder order = orders.findLockedByCode(orderCode).orElse(null);
        Instant now = clock.instant();
        if (order == null || order.getStatus() == PurchaseOrderStatus.PAGADA
                || order.getExpiresAt() == null || !order.getExpiresAt().isBefore(now)) {
            return 0;
        }
        List<EventBooking> held = bookings.findLockedByOrderId(order.getId()).stream()
                .filter(b -> HELD.contains(b.getStatus())).toList();
        held.stream().map(b -> b.getOccurrence().getId()).distinct().sorted().forEach(occurrences::findLockedById);
        for (EventBooking booking : held) {
            EventOccurrence occurrence = booking.getOccurrence();
            if (occurrence.getReservedSpots() < booking.getQuantity()) {
                log.warn("Disponibilidad inconsistente al liberar {}: reservadas={}, a liberar={}",
                        booking.getCode(), occurrence.getReservedSpots(), booking.getQuantity());
            }
            occurrence.setReservedSpots(Math.max(0, occurrence.getReservedSpots() - booking.getQuantity()));
            BookingStatusHistory change = new BookingStatusHistory();
            change.setBookingType(PurchaseItemType.EVENTO);
            change.setBookingId(booking.getId());
            change.setPreviousStatus(booking.getStatus().name());
            change.setNewStatus(BookingStatus.CANCELADA.name());
            change.setReason(REASON);
            change.setCreatedAt(now);
            history.save(change);
            booking.setStatus(BookingStatus.CANCELADA);
            booking.setCancelledAt(now);
        }
        if (order.getStatus() == PurchaseOrderStatus.ABIERTA) {
            order.setStatus(PurchaseOrderStatus.EXPIRADA);
        }
        return held.size();
    }
}

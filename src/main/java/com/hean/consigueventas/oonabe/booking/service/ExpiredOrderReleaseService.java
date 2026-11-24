package com.hean.consigueventas.oonabe.booking.service;

import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.notification.domain.NotificationType;
import com.hean.consigueventas.oonabe.notification.service.NotificationService;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Releases the spots of bookings whose deadline passed: a request nobody answered before the event
 * starts, or an accepted booking not paid in time. A confirmed booking is never released.
 */
@Service
@RequiredArgsConstructor
public class ExpiredOrderReleaseService {
    static final String REASON = "Reserva vencida sin completarse";
    private static final Set<BookingStatus> HELD = Set.of(BookingStatus.PENDIENTE, BookingStatus.ACEPTADA);

    private final PurchaseOrderRepository orders;
    private final EventBookingRepository bookings;
    private final EventOccurrenceRepository occurrences;
    private final BookingTransitions transitions;
    private final NotificationService notifications;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<String> findReleasableBookingCodes() {
        return bookings.findCodesWithExpiredHolds(clock.instant(), HELD);
    }

    /** Returns whether the booking was released; false when it was paid or released meanwhile. */
    @Transactional
    public boolean release(String bookingCode) {
        var reference = bookings.findReferenceByCode(bookingCode).orElse(null);
        if (reference == null) {
            return false;
        }
        // Same locking order as payment: order, booking(s), occurrence(s).
        PurchaseOrder order = null;
        if (reference.getOrderCode() != null) {
            order = orders.findLockedByCode(reference.getOrderCode()).orElse(null);
            if (order != null) {
                bookings.findLockedByOrderId(order.getId());
            }
        }
        EventBooking booking = bookings.findLockedByCode(bookingCode).orElse(null);
        Instant now = clock.instant();
        if (booking == null || !HELD.contains(booking.getStatus()) || !isExpired(booking, order, now)) {
            return false;
        }
        occurrences.findLockedById(reference.getOccurrenceId());
        transitions.cancel(booking, order, null, REASON, PurchaseOrderStatus.EXPIRADA, now);
        notifications.notifyCustomer(booking, NotificationType.RESERVA_VENCIDA);
        notifications.notifyProfessional(booking, NotificationType.RESERVA_EXPIRADA);
        return true;
    }

    private boolean isExpired(EventBooking booking, PurchaseOrder order, Instant now) {
        Instant deadline = booking.getExpiresAt() != null ? booking.getExpiresAt() : booking.getOccurrence().getStartsAt();
        // Orders created before the unified flow still carry their own expiry.
        return deadline.isBefore(now) || order != null && order.getExpiresAt() != null && order.getExpiresAt().isBefore(now);
    }
}

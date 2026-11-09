package com.hean.consigueventas.oonabe.booking.service;

import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
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
import java.util.Set;

/** Acciones del cliente sobre sus reservas. Una reserva pagada no se cancela ni se reembolsa. */
@Service
@RequiredArgsConstructor
public class ClientBookingService {
    static final String REASON = "Cancelada por el cliente";
    private static final Set<BookingStatus> CANCELLABLE = Set.of(BookingStatus.PENDIENTE, BookingStatus.ACEPTADA);

    private final EventBookingRepository bookings;
    private final PurchaseOrderRepository orders;
    private final EventOccurrenceRepository occurrences;
    private final BookingTransitions transitions;
    private final NotificationService notifications;
    private final Clock clock;

    @Transactional
    public void cancel(Long userId, String code) {
        var reference = bookings.findCustomerReference(code, userId).orElseThrow(this::notFound);
        // Same locking order as payment: order, booking(s), occurrence(s).
        PurchaseOrder order = null;
        if (reference.getOrderCode() != null) {
            order = orders.findLockedByCode(reference.getOrderCode()).orElseThrow(this::notFound);
            bookings.findLockedByOrderId(order.getId());
        }
        EventBooking booking = bookings.findLockedByCode(code).orElseThrow(this::notFound);
        if (!CANCELLABLE.contains(booking.getStatus())
                || order != null && order.getStatus() == PurchaseOrderStatus.PAGADA) {
            throw new BookingConflictException("Esta reserva ya no se puede cancelar. Recarga los datos.");
        }
        occurrences.findLockedById(reference.getOccurrenceId()).orElseThrow(this::notFound);
        Instant now = clock.instant();
        transitions.cancel(booking, order, booking.getCustomer().getUser(), REASON, PurchaseOrderStatus.CANCELADA, now);
        notifications.notifyCustomer(booking, NotificationType.SOLICITUD_CANCELADA);
        notifications.notifyProfessional(booking, NotificationType.CANCELACION_CLIENTE);
    }

    private ResourceNotFoundException notFound() {
        return new ResourceNotFoundException("Reserva no encontrada.");
    }
}

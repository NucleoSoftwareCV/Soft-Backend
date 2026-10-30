package com.hean.consigueventas.oonabe.booking.service;

import com.hean.consigueventas.oonabe.booking.dto.ProfessionalBookingDtos.*;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.entity.BookingStatusHistory;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.booking.repository.*;
import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderItemRepository;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessionalBookingService {
    private final EventBookingRepository bookings;
    private final EventAttendeeRepository attendees;
    private final BookingStatusHistoryRepository history;
    private final PurchaseOrderRepository orders;
    private final PurchaseOrderItemRepository orderItems;
    private final EventOccurrenceRepository occurrences;
    private final UserRepository users;
    private final Clock clock;

    public PagedResponse<Summary> list(Long userId, BookingStatus status, Long eventId, Long occurrenceId, int page, int size) {
        Specification<EventBooking> scope = (root, query, cb) ->
                cb.equal(root.get("occurrence").get("event").get("specialist").get("user").get("id"), userId);
        if (status != null) scope = scope.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (eventId != null) scope = scope.and((root, query, cb) -> cb.equal(root.get("occurrence").get("event").get("id"), eventId));
        if (occurrenceId != null) scope = scope.and((root, query, cb) -> cb.equal(root.get("occurrence").get("id"), occurrenceId));
        return PagedResponse.from(bookings.findAll(scope,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"))).map(this::summary));
    }

    public Detail detail(Long userId, String code) {
        return detail(bookings.findOwnedByCode(code, userId).orElseThrow(this::notFound));
    }

    @Transactional
    public Detail decide(Long userId, String code, Decision decision) {
        var reference = bookings.findOwnedReference(code, userId).orElseThrow(this::notFound);
        // Same locking order as payment: order, booking(s), occurrence(s).
        PurchaseOrder order = null;
        boolean orderClosed = false;
        if (reference.getOrderCode() != null) {
            order = orders.findLockedByCode(reference.getOrderCode()).orElseThrow(this::notFound);
            if (order.getStatus() == PurchaseOrderStatus.PAGADA) {
                throw new BookingConflictException("La reserva ya tiene un pago aprobado. Recarga los datos.");
            }
            orderClosed = order.getStatus() != PurchaseOrderStatus.ABIERTA
                    || order.getExpiresAt() != null && order.getExpiresAt().isBefore(clock.instant());
            bookings.findLockedByOrderId(order.getId());
        }
        EventBooking booking = bookings.findOwnedLockedByCode(code, userId).orElseThrow(this::notFound);
        if (booking.getStatus() != BookingStatus.PENDIENTE) {
            throw new BookingConflictException("La reserva ya no está pendiente. Recarga los datos.");
        }
        if (decision.status() != BookingStatus.CONFIRMADA && decision.status() != BookingStatus.CANCELADA) {
            throw new IllegalArgumentException("Estado de decisión no permitido");
        }
        if (decision.status() == BookingStatus.CANCELADA
                && (decision.reason() == null || decision.reason().isBlank())) {
            throw new IllegalArgumentException("El motivo es obligatorio al rechazar");
        }
        var occurrence = occurrences.findLockedById(reference.getOccurrenceId()).orElseThrow(this::notFound);
        if (decision.status() == BookingStatus.CONFIRMADA) {
            // An expired or cancelled order will never be paid; it can only be rejected to free its spots.
            if (orderClosed) {
                throw new BookingConflictException("La orden del cliente expiró o fue cancelada. Solo puedes rechazar la reserva.");
            }
            if (occurrence.getStatus() != EventOccurrenceStatus.PROGRAMADA || !occurrence.getStartsAt().isAfter(clock.instant())) {
                throw new BookingConflictException("La fecha ya no está disponible para aceptar reservas.");
            }
        } else {
            if (occurrence.getReservedSpots() < booking.getQuantity()) {
                throw new BookingConflictException("La disponibilidad es inconsistente. No se modificó la reserva.");
            }
            occurrence.setReservedSpots(occurrence.getReservedSpots() - booking.getQuantity());
            booking.setCancelledAt(clock.instant());
        }
        BookingStatusHistory change = new BookingStatusHistory();
        change.setBookingType(PurchaseItemType.EVENTO);
        change.setBookingId(booking.getId());
        change.setPreviousStatus(booking.getStatus().name());
        change.setNewStatus(decision.status().name());
        change.setChangedBy(users.getReferenceById(userId));
        change.setReason(decision.reason() == null ? null : decision.reason().strip());
        change.setCreatedAt(clock.instant());
        booking.setStatus(decision.status());
        history.save(change);
        if (decision.status() == BookingStatus.CANCELADA && order != null && !orderClosed) {
            excludeFromOpenOrder(order, booking);
        }
        // Flush also makes the updated occurrence version visible in the response transaction.
        bookings.flush();
        return detail(booking);
    }

    /** The rest of the order stays payable without the rejected booking; an order left empty is cancelled. */
    private void excludeFromOpenOrder(PurchaseOrder order, EventBooking rejected) {
        order.setSubtotal(order.getSubtotal().subtract(rejected.getTotalAmount()));
        order.setTotalAmount(order.getTotalAmount().subtract(rejected.getTotalAmount()));
        Set<Long> cancelledItems = bookings.findLockedByOrderId(order.getId()).stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELADA && b.getOrderItem() != null)
                .map(b -> b.getOrderItem().getId()).collect(Collectors.toSet());
        if (orderItems.findByOrderIdOrderByIdAsc(order.getId()).stream().allMatch(i -> cancelledItems.contains(i.getId()))) {
            order.setStatus(PurchaseOrderStatus.CANCELADA);
        }
    }

    private Summary summary(EventBooking b) {
        var event = b.getOccurrence().getEvent();
        return new Summary(b.getCode(), b.getCustomer().getFirstNames() + " " + b.getCustomer().getLastNames(),
                event.getId(), event.getTitle(), b.getOccurrence().getId(), b.getOccurrence().getStartsAt(),
                b.getQuantity(), b.getTotalAmount(), b.getCurrency(), b.getStatus(), b.getCreatedAt());
    }

    private Detail detail(EventBooking b) {
        var occurrence = b.getOccurrence();
        return new Detail(summary(b), b.getCustomer().getUser().getEmail(), b.getCustomer().getPhone(),
                occurrence.getCapacity(), occurrence.getReservedSpots(), occurrence.getCapacity() - occurrence.getReservedSpots(),
                attendees.findByEventBookingId(b.getId()).stream()
                        .map(a -> new Attendee(a.getId(), a.getAttendeeName(), a.getAttendeeEmail(), a.getAttendanceStatus().name())).toList(),
                history.findByBookingTypeAndBookingIdOrderByCreatedAtAscIdAsc(PurchaseItemType.EVENTO, b.getId()).stream()
                        .map(h -> new History(h.getPreviousStatus(), h.getNewStatus(),
                                h.getChangedBy() == null ? null : h.getChangedBy().getUsername(), h.getReason(), h.getCreatedAt())).toList());
    }

    private ResourceNotFoundException notFound() {
        return new ResourceNotFoundException("Reserva no encontrada.");
    }
}

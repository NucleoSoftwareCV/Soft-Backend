package com.hean.consigueventas.oonabe.booking.service;

import com.hean.consigueventas.oonabe.booking.entity.BookingStatusHistory;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.repository.BookingStatusHistoryRepository;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.campaign.service.CampaignEntryService;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.enums.PurchaseItemType;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderItemRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Cambios de estado comunes a profesional, cliente, pago y vencimiento. Quien llama ya tiene los bloqueos. */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingTransitions {
    private final BookingStatusHistoryRepository history;
    private final EventBookingRepository bookings;
    private final PurchaseOrderItemRepository orderItems;
    private final CampaignEntryService campaignEntries;

    /** El historial sin usuario identifica las cancelaciones del sistema (vencimiento). */
    public static String cancellationCause(BookingStatusHistory cancellation, Long customerUserId) {
        if (cancellation.getChangedBy() == null) {
            return "EXPIRADA";
        }
        return cancellation.getChangedBy().getId().equals(customerUserId) ? "CLIENTE" : "RECHAZADA";
    }

    public void changeStatus(EventBooking booking, BookingStatus status, User changedBy, String reason, Instant now) {
        BookingStatusHistory change = new BookingStatusHistory();
        change.setBookingType(PurchaseItemType.EVENTO);
        change.setBookingId(booking.getId());
        change.setPreviousStatus(booking.getStatus().name());
        change.setNewStatus(status.name());
        change.setChangedBy(changedBy);
        change.setReason(reason);
        change.setCreatedAt(now);
        booking.setStatus(status);
        history.save(change);
    }

    /** Libera la plaza y saca la reserva de su orden; si la orden queda vacía pasa a emptyOrderStatus. */
    public void cancel(EventBooking booking, PurchaseOrder order, User changedBy, String reason,
            PurchaseOrderStatus emptyOrderStatus, Instant now) {
        EventOccurrence occurrence = booking.getOccurrence();
        if (occurrence.getReservedSpots() < booking.getQuantity()) {
            log.warn("Disponibilidad inconsistente al liberar {}: reservadas={}, a liberar={}",
                    booking.getCode(), occurrence.getReservedSpots(), booking.getQuantity());
        }
        occurrence.setReservedSpots(Math.max(0, occurrence.getReservedSpots() - booking.getQuantity()));
        booking.setCancelledAt(now);
        changeStatus(booking, BookingStatus.CANCELADA, changedBy, reason, now);
        campaignEntries.voidForBookings(List.of(booking), reason, now);
        if (order != null && order.getStatus() == PurchaseOrderStatus.ABIERTA) {
            excludeFromOpenOrder(order, booking, emptyOrderStatus);
        }
    }

    /** El resto de la orden sigue pagable sin la reserva cancelada. */
    private void excludeFromOpenOrder(PurchaseOrder order, EventBooking cancelled, PurchaseOrderStatus emptyOrderStatus) {
        order.setSubtotal(order.getSubtotal().subtract(cancelled.getTotalAmount()));
        order.setTotalAmount(order.getTotalAmount().subtract(cancelled.getTotalAmount()));
        Set<Long> cancelledItems = bookings.findLockedByOrderId(order.getId()).stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELADA && b.getOrderItem() != null)
                .map(b -> b.getOrderItem().getId()).collect(Collectors.toSet());
        if (orderItems.findByOrderIdOrderByIdAsc(order.getId()).stream().allMatch(i -> cancelledItems.contains(i.getId()))) {
            order.setStatus(emptyOrderStatus);
        }
    }
}

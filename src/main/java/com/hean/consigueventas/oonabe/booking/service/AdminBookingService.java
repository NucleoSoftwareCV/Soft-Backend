package com.hean.consigueventas.oonabe.booking.service;

import com.hean.consigueventas.oonabe.booking.dto.AdminBookingDtos.Row;
import com.hean.consigueventas.oonabe.booking.entity.BookingStatusHistory;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.repository.BookingStatusHistoryRepository;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.enums.PurchaseItemType;
import com.hean.consigueventas.oonabe.payment.domain.PaymentStatus;
import com.hean.consigueventas.oonabe.payment.entity.Payment;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Supervisión de solo lectura: la gestión de cada reserva es del profesional. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBookingService {
    private final EventBookingRepository bookings;
    private final BookingStatusHistoryRepository history;
    private final PaymentRepository payments;

    public PagedResponse<Row> list(BookingStatus status, Instant from, Instant to, int page, int size) {
        Specification<EventBooking> filter = (root, query, cb) -> cb.conjunction();
        if (status != null) filter = filter.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (from != null) filter = filter.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        if (to != null) filter = filter.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), to));
        Page<EventBooking> found = bookings.findAll(filter,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));

        List<Long> cancelledIds = found.stream().filter(b -> b.getStatus() == BookingStatus.CANCELADA)
                .map(EventBooking::getId).toList();
        Map<Long, BookingStatusHistory> cancellations = cancelledIds.isEmpty() ? Map.of()
                : history.findByBookingTypeAndNewStatusAndBookingIdIn(
                        PurchaseItemType.EVENTO, BookingStatus.CANCELADA.name(), cancelledIds).stream()
                .collect(Collectors.toMap(BookingStatusHistory::getBookingId, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(BookingStatusHistory::getCreatedAt))));
        List<Long> orderIds = found.stream().map(this::order).filter(Objects::nonNull).map(PurchaseOrder::getId).distinct().toList();
        Map<Long, Payment> approved = orderIds.isEmpty() ? Map.of()
                : payments.findByOrderIdInAndStatus(orderIds, PaymentStatus.APROBADO).stream()
                .collect(Collectors.toMap(p -> p.getOrder().getId(), Function.identity(), (a, b) -> a));

        return PagedResponse.from(found.map(b -> row(b, cancellations.get(b.getId()), approved)));
    }

    private Row row(EventBooking b, BookingStatusHistory cancellation, Map<Long, Payment> approved) {
        var event = b.getOccurrence().getEvent();
        var customer = b.getCustomer();
        PurchaseOrder order = order(b);
        Payment payment = order == null ? null : approved.get(order.getId());
        return new Row(b.getCode(), order == null ? null : order.getCode(), order == null ? null : order.getStatus(),
                customer.getFirstNames() + " " + customer.getLastNames(), customer.getUser().getEmail(),
                event.getSpecialist().getPublicName(), event.getId(), event.getTitle(), b.getOccurrence().getStartsAt(),
                b.getQuantity(), b.getTotalAmount(), b.getCurrency(), b.getStatus(),
                b.getStatus() == BookingStatus.ACEPTADA ? b.getExpiresAt() : null,
                payment == null ? null : payment.getProvider(), payment == null ? null : payment.getProcessedAt(),
                b.getCancelledAt(),
                cancellation == null ? null : BookingTransitions.cancellationCause(cancellation, customer.getUser().getId()),
                cancellation == null ? null : cancellation.getReason(), b.getCreatedAt());
    }

    private PurchaseOrder order(EventBooking booking) {
        return booking.getOrderItem() == null ? null : booking.getOrderItem().getOrder();
    }
}

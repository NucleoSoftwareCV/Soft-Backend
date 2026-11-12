package com.hean.consigueventas.oonabe.booking.dto;

import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public final class AdminBookingDtos {
    private AdminBookingDtos() {}

    @Schema(description = "Reserva con su actividad de pago y cancelación, para supervisión")
    public record Row(String code, String orderCode, PurchaseOrderStatus orderStatus, String customerName, String customerEmail,
            String professionalName, Long eventId, String eventTitle, Instant occurrenceStartsAt, short quantity,
            BigDecimal totalAmount, String currency, BookingStatus status, Instant paymentDeadline,
            String paymentMethod, Instant paidAt, Instant cancelledAt, String cancellationCause,
            String cancellationReason, Instant createdAt) {}
}

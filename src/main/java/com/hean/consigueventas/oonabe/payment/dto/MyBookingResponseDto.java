package com.hean.consigueventas.oonabe.payment.dto;

import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyBookingResponseDto {
    private String code;
    /** Código con el que se paga la reserva. */
    private String orderCode;
    private Long occurrenceId;
    /** Solo en reservas aceptadas: límite para pagar antes de que se libere la plaza. */
    private Instant paymentDeadline;
    private BookingStatus status;
    private Short quantity;
    private BigDecimal totalAmount;
    private String currency;
    private Instant createdAt;
    private Long eventId;
    private String eventTitle;
    private Instant occurrenceStartsAt;
    private EventModality modality;
    private String locationName;
    private String cityName;
    private Instant cancelledAt;
    /** RECHAZADA when the professional rejected it, CLIENTE when the client cancelled it, EXPIRADA when its deadline passed. */
    private String cancellationCause;
    private String cancellationReason;
}

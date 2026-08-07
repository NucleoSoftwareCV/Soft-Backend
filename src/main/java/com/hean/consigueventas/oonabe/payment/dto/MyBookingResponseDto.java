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
}

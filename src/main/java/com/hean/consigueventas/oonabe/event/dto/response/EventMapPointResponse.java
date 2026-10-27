package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import java.math.BigDecimal;
import java.time.Instant;

/** Datos públicos mínimos para situar una sesión disponible en el mapa. */
public record EventMapPointResponse(
        Long occurrenceId,
        Long eventId,
        String title,
        BigDecimal priceFrom,
        String currency,
        Instant startsAt,
        Instant endsAt,
        String locationName,
        String address,
        String cityName,
        BigDecimal latitude,
        BigDecimal longitude,
        String summary,
        Long categoryId,
        String categoryName,
        Long experienceTypeId,
        String experienceTypeName,
        Long specialistId,
        String organizerName,
        EventModality modality,
        boolean isRecurring
) {
}

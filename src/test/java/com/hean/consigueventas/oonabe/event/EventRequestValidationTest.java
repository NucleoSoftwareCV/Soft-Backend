package com.hean.consigueventas.oonabe.event;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventPaymentMethod;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.request.MeetingLinkUpsertRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void eventRejectsNegativePriceInvalidCurrencyAndAgeOutsideRange() {
        EventUpsertRequest request = new EventUpsertRequest(
                "Yoga",
                "Resumen",
                "Descripcion",
                List.of(),
                List.of(),
                List.of(),
                EventModality.ONLINE,
                new BigDecimal("-1.00"),
                "EU",
                (short) 121,
                false,
                1L,
                EventPaymentMethod.WHATSAPP,
                1L,
                1L);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("priceFrom", "currency", "minimumAge");
    }

    @Test
    void occurrenceRequiresEndAfterStart() {
        Instant startsAt = Instant.parse("2026-07-20T10:00:00Z");
        EventOccurrenceRequest request = new EventOccurrenceRequest(
                startsAt,
                startsAt,
                10,
                null,
                null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("dateRangeValid");
    }

    @Test
    void meetingLinkRequiresValidUrl() {
        MeetingLinkUpsertRequest request = new MeetingLinkUpsertRequest(
                "ZOOM",
                "not-a-url",
                null,
                null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("meetingUrl");
    }
}

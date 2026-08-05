package com.hean.consigueventas.oonabe.home.mapper;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.home.dto.response.HomeEventCardResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HomeEventCardMapperTest {

    private final HomeEventCardMapper mapper = Mappers.getMapper(HomeEventCardMapper.class);

    @Test
    void mapsFreeRecurringEventWithoutAddingCardDetailFields() {
        EventCardResponse source = new EventCardResponse(
                1L,
                "Yoga semanal",
                "Resumen que no necesita la home",
                EventModality.ONLINE,
                BigDecimal.ZERO,
                "EUR",
                10L,
                "Yoga",
                20L,
                "Profesional",
                "https://example.com/professional.jpg",
                "https://example.com/event.jpg",
                Instant.parse("2026-08-01T10:00:00Z"),
                Instant.parse("2026-08-01T11:00:00Z"),
                null,
                30L,
                "Clases",
                "clases",
                true
        );

        HomeEventCardResponse response = mapper.toResponse(source);

        assertThat(response.priceFrom()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.recurrenceLabel()).isEqualTo("RECURRENTE");
        assertThat(response.modality()).isEqualTo(EventModality.ONLINE);
    }
}

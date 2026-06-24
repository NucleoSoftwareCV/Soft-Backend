package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Datos detallados de un evento, incluyendo organizador y sus ocurrencias/horarios")
public record EventDetailResponse(
        @Schema(description = "ID del evento")
        Long id,

        @Schema(description = "Título del evento")
        String title,

        @Schema(description = "Resumen corto")
        String summary,

        @Schema(description = "Descripción completa")
        String description,

        @Schema(description = "Modalidad (ONLINE o PRESENCIAL)")
        EventModality modality,

        @Schema(description = "Precio inicial")
        BigDecimal priceFrom,

        @Schema(description = "Moneda")
        String currency,

        @Schema(description = "Edad mínima")
        Short minimumAge,

        @Schema(description = "¿Destacado?")
        Boolean featured,

        @Schema(description = "ID de la categoría")
        Long categoryId,

        @Schema(description = "Nombre de la categoría")
        String categoryName,

        @Schema(description = "Organizador del evento")
        EventOrganizerResponse organizer,

        @Schema(description = "Horarios y ubicaciones del evento")
        List<EventOccurrenceResponse> occurrences,

        @Schema(description = "Fecha de creación")
        Instant createdAt,

        @Schema(description = "Fecha de actualización")
        Instant updatedAt
) {
}

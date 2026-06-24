package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Respuesta con los datos del evento creado")
public record EventResponse(
        @Schema(description = "ID del evento")
        Long id,

        @Schema(description = "Título del evento")
        String title,

        @Schema(description = "Resumen del evento")
        String summary,

        @Schema(description = "Descripción del evento")
        String description,

        @Schema(description = "Modalidad del evento")
        EventModality modality,

        @Schema(description = "Precio")
        BigDecimal priceFrom,

        @Schema(description = "Moneda")
        String currency,

        @Schema(description = "Edad mínima")
        Short minimumAge,

        @Schema(description = "¿Está destacado?")
        Boolean featured,

        @Schema(description = "ID de la categoría")
        Long categoryId,

        @Schema(description = "Nombre de la categoría")
        String categoryName,

        @Schema(description = "Fecha de creación")
        Instant createdAt,

        @Schema(description = "Fecha de actualización")
        Instant updatedAt
) {
}
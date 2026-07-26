package com.hean.consigueventas.oonabe.interaction.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Evento guardado como favorito por el cliente")
public record EventFavoriteResponse(

        @Schema(description = "ID del favorito")
        Long id,

        @Schema(description = "ID del evento")
        Long eventId,

        @Schema(description = "Título del evento")
        String title,

        @Schema(description = "Resumen del evento")
        String summary,

        @Schema(description = "Nombre de la categoría del evento")
        String categoryName,

        @Schema(description = "Modalidad del evento")
        EventModality modality,

        @Schema(description = "Tipo de evento")
        EventType eventType,

        @Schema(description = "Precio base del evento")
        BigDecimal priceFrom,

        @Schema(description = "Moneda del evento")
        String currency,

        @Schema(description = "Fecha en la que fue guardado")
        Instant savedAt
) {
}
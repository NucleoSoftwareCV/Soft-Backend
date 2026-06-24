package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Datos resumidos de un evento para mostrar en tarjetas/listados")
public record EventCardResponse(
        @Schema(description = "ID del evento")
        Long id,

        @Schema(description = "Título del evento")
        String title,

        @Schema(description = "Resumen corto")
        String summary,

        @Schema(description = "Modalidad (ONLINE o PRESENCIAL)")
        EventModality modality,

        @Schema(description = "Precio inicial")
        BigDecimal priceFrom,

        @Schema(description = "Moneda")
        String currency,

        @Schema(description = "ID de la categoría")
        Long categoryId,

        @Schema(description = "Nombre de la categoría")
        String categoryName,

        @Schema(description = "ID del organizador")
        Long organizerId,

        @Schema(description = "Nombre del organizador")
        String organizerName,

        @Schema(description = "URL de la foto del organizador")
        String organizerPhotoUrl,

        @Schema(description = "Fecha de inicio del primer horario programado")
        Instant startsAt,

        @Schema(description = "Fecha de fin del primer horario programado")
        Instant endsAt,

        @Schema(description = "Ciudad (para eventos presenciales)")
        String cityName,

        @Schema(description = "Tipo de experiencia (TALLER, RETIRO, CLASE, etc.)")
        EventType eventType,

        @Schema(description = "¿Es recurrente?")
        Boolean isRecurring
) {
}

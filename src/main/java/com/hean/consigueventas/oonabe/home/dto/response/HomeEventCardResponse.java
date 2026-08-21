package com.hean.consigueventas.oonabe.home.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Datos mínimos de un evento para una tarjeta de la página principal")
public record HomeEventCardResponse(
        @Schema(description = "ID del evento")
        Long id,

        @Schema(description = "Título del evento")
        String title,

        @Schema(description = "URL de la imagen de portada", nullable = true)
        String coverImageUrl,

        @Schema(description = "Fecha de inicio de la próxima ocurrencia")
        Instant startsAt,

        @Schema(description = "Modalidad del evento")
        EventModality modality,

        @Schema(description = "Ciudad para eventos presenciales", nullable = true)
        String cityName,

        @Schema(description = "Nombre público del organizador")
        String organizerName,

        @Schema(description = "Foto del organizador", nullable = true)
        String organizerPhotoUrl,

        @Schema(description = "Precio mínimo; cero representa un evento gratuito")
        BigDecimal priceFrom,

        @Schema(description = "Moneda ISO 4217")
        String currency,

        @Schema(
                description = "Etiqueta de recurrencia disponible; null cuando el evento no es recurrente",
                example = "RECURRENTE",
                nullable = true
        )
        String recurrenceLabel
) {
}

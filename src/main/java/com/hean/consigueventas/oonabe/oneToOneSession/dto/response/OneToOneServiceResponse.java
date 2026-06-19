package com.hean.consigueventas.oonabe.oneToOneSession.dto.response;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Detalles del servicio de sesión 1-a-1.")
public record OneToOneServiceResponse(
        @Schema(description = "ID del servicio.")
        Long id,

        @Schema(description = "ID del especialista.")
        Long specialistId,

        @Schema(description = "Nombre público del especialista.")
        String specialistName,

        @Schema(description = "Slug amigable para URLs.")
        String slug,

        @Schema(description = "Título de la sesión.")
        String title,

        @Schema(description = "Descripción detallada.")
        String description,

        @Schema(description = "Duración en minutos.")
        Integer durationMinutes,

        @Schema(description = "Modalidad (ONLINE, PRESENCIAL, AMBAS).")
        SessionModality modality,

        @Schema(description = "ID de la ubicación física (puede ser nulo).")
        Long locationId,

        @Schema(description = "Nombre de la ubicación física (puede ser nulo).")
        String locationName,

        @Schema(description = "Precio.")
        BigDecimal price,

        @Schema(description = "Moneda del precio.")
        String currency,

        @Schema(description = "Estado (BORRADOR, PUBLICADO, OCULTO).")
        PublicationStatus status,

        @Schema(description = "Fecha de creación.")
        Instant createdAt,

        @Schema(description = "Fecha de última actualización.")
        Instant updatedAt
) {
}

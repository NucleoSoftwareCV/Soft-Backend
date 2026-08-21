package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.masterdata.dto.response.LocationResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Respuesta con los datos de la ocurrencia del evento")
public record EventOccurrenceResponse(
        @Schema(description = "ID de la ocurrencia")
        Long id,

        @Schema(description = "Fecha de inicio")
        Instant startsAt,

        @Schema(description = "Fecha de fin")
        Instant endsAt,

        @Schema(description = "Capacidad máxima")
        Integer capacity,

        @Schema(description = "Espacios reservados")
        Integer reservedSpots,

        @Schema(description = "Espacios disponibles")
        Integer availableSpots,

        @Schema(description = "¿Está agotado?")
        Boolean soldOut,

        @Schema(description = "Estado de la ocurrencia")
        String status,

        @Schema(description = "Datos de ubicación (si es presencial)")
        LocationResponse location,

        @Schema(description = "Datos del enlace (si es virtual)")
        MeetingLinkResponse meetingLink
) {
}

package com.hean.consigueventas.oonabe.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Ocurrencia de un evento para el calendario publico del perfil de un especialista")
public record EventOccurrenceCalendarResponse(

        @Schema(description = "ID de la ocurrencia")
        Long occurrenceId,

        @Schema(description = "ID del evento")
        Long eventId,

        @Schema(description = "Titulo del evento")
        String eventTitle,

        @Schema(description = "URL de la imagen de portada del evento")
        String coverImageUrl,

        @Schema(description = "Fecha y hora de inicio de esta ocurrencia")
        Instant startsAt
) {
}

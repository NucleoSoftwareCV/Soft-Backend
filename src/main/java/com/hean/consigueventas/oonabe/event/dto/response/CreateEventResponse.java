package com.hean.consigueventas.oonabe.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta completa de creación de evento con ocurrencia")
public record CreateEventResponse(
        @Schema(description = "Datos del evento")
        EventResponse event,

        @Schema(description = "Datos de la ocurrencia")
        EventOccurrenceResponse occurrence,

        @Schema(description = "Mensaje de confirmación")
        String message
) {
}
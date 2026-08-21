package com.hean.consigueventas.oonabe.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos completos para crear un evento con su ocurrencia.")
public record CreateEventUpsertRequest(
        @Schema(description = "Información general del evento.")
        @NotNull(message = "Los datos del evento son obligatorios")
        @Valid
        EventUpsertRequest event,


        @Schema(description = "Información de la ocurrencia.")
        @NotNull(message = "Los datos de la ocurrencia son obligatorios")
        @Valid
        EventOccurrenceRequest occurrence

) {
}

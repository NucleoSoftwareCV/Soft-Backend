package com.hean.consigueventas.oonabe.event.dto.request;

import com.hean.consigueventas.oonabe.masterdata.dto.request.LocationUpsertRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Datos de la fecha del evento.")
public record EventOccurrenceRequest(
        @Schema(description = "Fecha y hora de inicio.")
        @NotNull(message = "La fecha de inicio es obligatoria")
        Instant startsAt,


        @Schema(description = "Fecha y hora de fin.")
        @NotNull(message = "La fecha final es obligatoria")
        Instant endsAt,


        @Schema(description = "Capacidad máxima.")
        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1)
        Integer capacity,


        @Schema(description = "Ubicación presencial")
        @Valid
        LocationUpsertRequest location,


        @Schema(description = "Enlace virtual")
        @Valid
        MeetingLinkUpsertRequest meetingLink
) {
}

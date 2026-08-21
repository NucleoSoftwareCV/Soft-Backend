package com.hean.consigueventas.oonabe.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los datos del enlace de reunión")
public record MeetingLinkResponse(
        @Schema(description = "ID del enlace")
        Long id,

        @Schema(description = "Plataforma")
        String platform,

        @Schema(description = "URL de la reunión")
        String meetingUrl,

        @Schema(description = "ID de la reunión")
        String meetingId,

        @Schema(description = "Contraseña")
        String password
) {
}
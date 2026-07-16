package com.hean.consigueventas.oonabe.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmacion de participacion en Conocer gente.")
public record MatchSubmissionResponse(
        @Schema(example = "Gracias por participar. M\u00e1s adelante te enviaremos por WhatsApp la informaci\u00f3n de tu match.")
        String message
) {
}

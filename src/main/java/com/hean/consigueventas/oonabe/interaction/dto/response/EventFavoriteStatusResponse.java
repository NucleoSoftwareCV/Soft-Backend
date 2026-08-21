package com.hean.consigueventas.oonabe.interaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de favorito de un evento")
public record EventFavoriteStatusResponse(

        @Schema(description = "ID del evento")
        Long eventId,

        @Schema(description = "Indica si el evento está guardado como favorito")
        Boolean favorite
) {
}
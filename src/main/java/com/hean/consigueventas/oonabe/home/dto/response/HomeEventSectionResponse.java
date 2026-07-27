package com.hean.consigueventas.oonabe.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Carrusel de eventos de la pagina principal")
public record HomeEventSectionResponse(
        @Schema(description = "Clave estable de la seccion")
        HomeEventSectionKey key,

        @Schema(description = "Titulo visible")
        String title,

        HomeEventSectionFiltersResponse viewAllFilters,

        @Schema(description = "Tarjetas de eventos, limitadas por el parametro limit")
        List<HomeEventCardResponse> events
) {
}

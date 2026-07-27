package com.hean.consigueventas.oonabe.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Secciones publicas de eventos para la pagina principal")
public record HomeEventSectionsResponse(
        @Schema(description = "Ciudad usada para seleccionar eventos presenciales")
        String cityName,

        List<HomeEventSectionResponse> sections
) {
}

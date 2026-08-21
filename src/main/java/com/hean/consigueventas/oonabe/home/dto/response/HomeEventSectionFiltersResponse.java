package com.hean.consigueventas.oonabe.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Filtros que permiten abrir la seccion completa en Explorar")
public record HomeEventSectionFiltersResponse(
        @Schema(description = "IDs de categorias combinados con OR")
        List<Long> categoryIds,

        @Schema(description = "Tipo de experiencia")
        Long experienceTypeId,

        @Schema(description = "Fecha inicial inclusiva")
        LocalDate dateFrom,

        @Schema(description = "Fecha final inclusiva")
        LocalDate dateTo,

        @Schema(description = "Ciudad seleccionada")
        String cityName,

        @Schema(description = "Indica si deben incluirse tambien eventos online")
        boolean includeOnline
) {
}

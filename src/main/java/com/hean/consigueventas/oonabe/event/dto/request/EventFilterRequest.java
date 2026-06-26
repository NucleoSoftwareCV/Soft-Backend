package com.hean.consigueventas.oonabe.event.dto.request;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Parámetros opcionales de filtro para el listado público de eventos.
 * Todos los campos son opcionales; si se omiten no se aplica ese filtro.
 */
@Schema(description = "Filtros opcionales para buscar eventos")
public record EventFilterRequest(

        @Schema(description = "Búsqueda por texto libre en título y resumen")
        String search,

        @Schema(description = "ID de la categoría")
        Long categoryId,

        @Schema(description = "Tipo de experiencia")
        EventType eventType,

        @Schema(description = "Modalidad del evento (ONLINE o PRESENCIAL)")
        EventModality modality,

        @Schema(description = "Nombre de la ciudad (para eventos presenciales)")
        String cityName,

        @Schema(description = "Precio mínimo. Usa 0 para filtrar eventos gratuitos")
        BigDecimal minPrice,

        @Schema(description = "Precio máximo")
        BigDecimal maxPrice,

        @Schema(description = "Fecha de inicio del rango de búsqueda (inclusive)")
        LocalDate dateFrom,

        @Schema(description = "Fecha de fin del rango de búsqueda (inclusive)")
        LocalDate dateTo,

        @Schema(description = "Hora de inicio del día (0-23). Ej: 6 para mañanas")
        Integer hourFrom,

        @Schema(description = "Hora de fin del día (0-23). Ej: 12 para mañanas")
        Integer hourTo,

        @Schema(description = "true = solo recurrentes, false = solo únicos, null = todos")
        Boolean isRecurring
) {
}

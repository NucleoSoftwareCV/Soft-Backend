package com.hean.consigueventas.oonabe.event.dto.request;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Parametros opcionales de filtro para el listado publico de eventos.
 * Todos los campos son opcionales; si se omiten no se aplica ese filtro.
 */
@Schema(description = "Filtros opcionales para buscar eventos")
public record EventFilterRequest(

        @Schema(description = "Busqueda por texto libre en titulo y resumen")
        String search,

        @Schema(description = "ID de la categoria")
        Long categoryId,

        @Schema(description = "IDs de categorias combinadas con OR")
        List<Long> categoryIds,

        @Schema(description = "Tipo de experiencia")
        Long experienceTypeId,

        @Schema(description = "Modalidad del evento (ONLINE o PRESENCIAL)")
        EventModality modality,

        @Schema(description = "Nombre de la ciudad (para eventos presenciales)")
        String cityName,

        @Schema(description = "Incluye eventos online cuando se filtra por ciudad")
        Boolean includeOnline,

        @Schema(description = "Precio minimo. Usa 0 para filtrar eventos gratuitos")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio minimo no puede ser negativo")
        BigDecimal minPrice,

        @Schema(description = "Precio maximo")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio maximo no puede ser negativo")
        BigDecimal maxPrice,

        @Schema(description = "Fecha de inicio del rango de busqueda (inclusive)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @Schema(description = "Fecha de fin del rango de busqueda (inclusive)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,

        @Schema(description = "Hora de inicio del dia (0-23). Ej: 6 para mananas")
        @Min(value = 0, message = "La hora de inicio debe estar entre 0 y 23")
        @Max(value = 23, message = "La hora de inicio debe estar entre 0 y 23")
        Integer hourFrom,

        @Schema(description = "Hora de fin del dia (0-23). Ej: 12 para mananas")
        @Min(value = 0, message = "La hora de fin debe estar entre 0 y 23")
        @Max(value = 23, message = "La hora de fin debe estar entre 0 y 23")
        Integer hourTo,

        @Schema(description = "true = solo recurrentes, false = solo unicos, null = todos")
        Boolean isRecurring,

        @Schema(description = "ID del especialista/organizador")
        Long specialistId
) {
        @AssertTrue(message = "La fecha final no puede ser anterior a la fecha inicial")
        @Schema(hidden = true)
        public boolean isDateRangeValid() {
                return dateFrom == null || dateTo == null || !dateTo.isBefore(dateFrom);
        }

        @AssertTrue(message = "La hora final no puede ser anterior a la hora inicial")
        @Schema(hidden = true)
        public boolean isHourRangeValid() {
                return hourFrom == null || hourTo == null || hourTo >= hourFrom;
        }

        @AssertTrue(message = "El precio maximo no puede ser menor que el precio minimo")
        @Schema(hidden = true)
        public boolean isPriceRangeValid() {
                return minPrice == null || maxPrice == null || maxPrice.compareTo(minPrice) >= 0;
        }

        @AssertTrue(message = "Usa categoryId o categoryIds, pero no ambos")
        @Schema(hidden = true)
        public boolean isCategoryFilterValid() {
                return categoryId == null || categoryIds == null || categoryIds.isEmpty();
        }
}

package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventPaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Datos detallados de un evento, incluyendo organizador y sus ocurrencias/horarios")
public record EventDetailResponse(
        @Schema(description = "ID del evento")
        Long id,

        @Schema(description = "Título del evento")
        String title,

        @Schema(description = "Resumen corto")
        String summary,

        @Schema(description = "Descripción completa")
        String description,

        @Schema(description = "Elementos incluidos en el evento")
        List<String> includes,

        @Schema(description = "Puntos destacados del evento")
        List<String> highlights,

        @Schema(description = "Elementos que debe traer la persona asistente")
        List<String> whatToBring,

        @Schema(description = "Modalidad (ONLINE o PRESENCIAL)")
        EventModality modality,

        @Schema(description = "Precio inicial")
        BigDecimal priceFrom,

        @Schema(description = "Moneda")
        String currency,

        @Schema(description = "Edad mínima")
        Short minimumAge,

        @Schema(description = "¿Destacado?")
        Boolean featured,

        @Schema(description = "ID de la categoría")
        Long categoryId,

        @Schema(description = "Método de pago del evento")
        EventPaymentMethod paymentMethod,

        @Schema(description = "Nombre de la categoría")
        String categoryName,

        @Schema(description = "Organizador del evento")
        EventOrganizerResponse organizer,

        @Schema(description = "Horarios y ubicaciones del evento")
        List<EventOccurrenceResponse> occurrences,

        @Schema(description = "Tipo de experiencia (TALLER, RETIRO, CLASE, etc.)")
        Long experienceTypeId,

        String eventType,

        String experienceTypeSlug,

        @Schema(description = "¿Es recurrente?")
        Boolean isRecurring,

        @Schema(description = "Fecha de creación")
        Instant createdAt,

        @Schema(description = "Fecha de actualización")
        Instant updatedAt,

        @Schema(description = "URL de la imagen de portada del evento")
        String coverImageUrl,

        @Schema(description = "Galeria de imagenes del evento, ordenadas")
        List<EventImageResponse> images
) {
}

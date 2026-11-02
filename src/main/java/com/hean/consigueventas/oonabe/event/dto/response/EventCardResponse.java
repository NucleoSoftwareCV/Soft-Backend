package com.hean.consigueventas.oonabe.event.dto.response;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventMood;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignBadgeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Datos resumidos de un evento para mostrar en tarjetas/listados")
public record EventCardResponse(
        @Schema(description = "ID del evento")
        Long id,

        @Schema(description = "Título del evento")
        String title,

        @Schema(description = "Resumen corto")
        String summary,

        @Schema(description = "Modalidad (ONLINE o PRESENCIAL)")
        EventModality modality,

        @Schema(description = "Precio inicial")
        BigDecimal priceFrom,

        @Schema(description = "Moneda")
        String currency,

        @Schema(description = "ID de la categoría")
        Long categoryId,

        @Schema(description = "Nombre de la categoría")
        String categoryName,

        @Schema(description = "ID del organizador")
        Long organizerId,

        @Schema(description = "Nombre del organizador")
        String organizerName,

        @Schema(description = "URL de la foto del organizador")
        String organizerPhotoUrl,

        @Schema(description = "URL de la imagen de portada del evento", nullable = true)
        String coverImageUrl,

        @Schema(description = "Fecha de inicio del primer horario programado")
        Instant startsAt,

        @Schema(description = "Fecha de fin del primer horario programado")
        Instant endsAt,

        @Schema(description = "Ciudad (para eventos presenciales)")
        String cityName,

        @Schema(description = "ID del tipo de experiencia configurado en el catalogo")
        Long experienceTypeId,

        @Schema(description = "Nombre publico del tipo de experiencia", example = "Talleres")
        String eventType,

        @Schema(description = "Slug del tipo de experiencia", example = "talleres")
        String experienceTypeSlug,

        @Schema(description = "¿Es recurrente?")
        Boolean isRecurring,

        @Schema(description = "Mood (RELAJACION | ENERGIA | CONECTAR)")
        EventMood mood,

        @Schema(description = "Campañas promocionales abiertas en las que participa el evento (vacía si ninguna)")
        List<CampaignBadgeResponse> campaignBadges
) {
    public EventCardResponse {
        campaignBadges = campaignBadges == null ? List.of() : List.copyOf(campaignBadges);
    }

    /** Tarjeta sin distintivos de campaña (listados que no los resuelven). */
    public EventCardResponse(
            Long id,
            String title,
            String summary,
            EventModality modality,
            BigDecimal priceFrom,
            String currency,
            Long categoryId,
            String categoryName,
            Long organizerId,
            String organizerName,
            String organizerPhotoUrl,
            String coverImageUrl,
            Instant startsAt,
            Instant endsAt,
            String cityName,
            Long experienceTypeId,
            String eventType,
            String experienceTypeSlug,
            Boolean isRecurring,
            EventMood mood) {
        this(id, title, summary, modality, priceFrom, currency, categoryId, categoryName, organizerId,
                organizerName, organizerPhotoUrl, coverImageUrl, startsAt, endsAt, cityName, experienceTypeId,
                eventType, experienceTypeSlug, isRecurring, mood, List.of());
    }
}

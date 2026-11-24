package com.hean.consigueventas.oonabe.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Distintivo de campaña promocional abierta en la que participa un evento")
public record CampaignBadgeResponse(
        @Schema(description = "Slug de la campaña", example = "sorteo-bienestar")
        String slug,

        @Schema(description = "Texto del distintivo", example = "Participa en el sorteo")
        String label,

        @Schema(description = "Título de la campaña", example = "Sorteamos un año de bienestar gratis")
        String title
) {
}

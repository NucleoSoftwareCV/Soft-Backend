package com.hean.consigueventas.oonabe.profileCliente.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Preferencias del perfil cliente")
public record ClientProfilePreferencesResponse(

        @Schema(description = "ID del perfil cliente")
        Long id,

        @Schema(description = "ID del usuario relacionado")
        Long userId,

        @Schema(description = "Nombre del cliente")
        String firstName,

        @Schema(description = "Apellido del cliente")
        String lastName,

        @Schema(description = "ID de la ciudad")
        Long cityId,

        @Schema(description = "Nombre de la ciudad")
        String cityName,

        @Schema(description = "Correo usado para comunicaciones")
        String communicationEmail,

        @Schema(description = "Número de WhatsApp")
        String whatsappPhone,

        @Schema(description = "Indica si recibe confirmaciones de eventos guardados")
        Boolean receiveSavedEventConfirmations,

        @Schema(description = "Indica si recibe recomendaciones personalizadas")
        Boolean receivePersonalizedRecommendations,

        @Schema(description = "Indica si recibe confirmaciones de reservas")
        Boolean receiveReservationConfirmations,

        @Schema(description = "Indica si recibe resumen semanal")
        Boolean receiveWeeklySummary,

        @Schema(description = "Categorías de interés del cliente")
        List<ClientInterestCategoryResponse> interestCategories
) {
}
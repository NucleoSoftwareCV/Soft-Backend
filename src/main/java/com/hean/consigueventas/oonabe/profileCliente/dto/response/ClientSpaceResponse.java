package com.hean.consigueventas.oonabe.profileCliente.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta principal del espacio del cliente")
public record ClientSpaceResponse(

        @Schema(description = "ID del perfil cliente")
        Long clientProfileId,

        @Schema(description = "ID del usuario relacionado")
        Long userId,

        @Schema(description = "Título de la sección")
        String title,

        @Schema(description = "Nombre del cliente")
        String firstName,

        @Schema(description = "Apellido del cliente")
        String lastName,

        @Schema(description = "Texto descriptivo del espacio del cliente")
        String description,

        @Schema(description = "Cantidad de próximas sesiones")
        Integer nextSessionCount,

        @Schema(description = "Cantidad de reservas activas")
        Integer activeReservationCount,

        @Schema(description = "Cantidad de eventos guardados como favoritos")
        Long favoriteEventCount,

        @Schema(description = "Cantidad de profesionales seguidos")
        Long followedProfessionalCount
) {
}
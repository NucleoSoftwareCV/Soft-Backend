package com.hean.consigueventas.oonabe.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos resumidos del organizador del evento")
public record EventOrganizerResponse(
        @Schema(description = "ID del especialista/organizador")
        Long id,

        @Schema(description = "Nombre público")
        String publicName,

        @Schema(description = "Biografía o resumen")
        String biography,

        @Schema(description = "URL de la foto de perfil")
        String photoUrl,

        @Schema(description = "Numero de WhatsApp publico del organizador", example = "+34600111222")
        String whatsappPhone
) {
}

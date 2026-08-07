package com.hean.consigueventas.oonabe.oneToOneSession.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos minimos de una sesion 1-a-1 para tarjetas/listados publicos.")
public record OneToOneServiceCardResponse(
        @Schema(description = "ID del servicio.", example = "13")
        Long id,

        @Schema(description = "Titulo de la sesion.", example = "Embarazo acompanamiento emocional")
        String title,

        @Schema(description = "Nombre publico del especialista.", example = "Ana Gomez")
        String specialistName,

        @Schema(description = "URL de la foto publica del especialista.")
        String specialistPhotoUrl,

        @Schema(description = "Precio.", example = "40.00")
        BigDecimal price,

        @Schema(description = "Moneda del precio.", example = "EUR")
        String currency,

        @Schema(description = "Duracion en minutos.", example = "90")
        Integer durationMinutes,

        @Schema(description = "URL de imagen para la tarjeta.", example = "https://images.unsplash.com/photo-1492725764893-90b379c2b6e7")
        String imageUrl
) {
}

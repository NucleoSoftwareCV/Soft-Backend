package com.hean.consigueventas.oonabe.oneToOneSession.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos minimos de una sesion 1-a-1 para tarjetas/listados publicos.")
public record OneToOneServiceCardResponse(
        @Schema(description = "ID del servicio.")
        Long id,

        @Schema(description = "Titulo de la sesion.")
        String title,

        @Schema(description = "Nombre publico del especialista.")
        String specialistName,

        @Schema(description = "Precio.")
        BigDecimal price,

        @Schema(description = "Moneda del precio.")
        String currency,

        @Schema(description = "Duracion en minutos.")
        Integer durationMinutes,

        @Schema(description = "URL de imagen para la tarjeta.")
        String imageUrl
) {
}

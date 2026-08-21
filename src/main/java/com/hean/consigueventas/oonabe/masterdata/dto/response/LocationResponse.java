package com.hean.consigueventas.oonabe.masterdata.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Respuesta con los datos de ubicación")
public record LocationResponse(
        @Schema(description = "ID de la ubicación")
        Long id,

        @Schema(description = "Nombre de la ubicación")
        String name,

        @Schema(description = "Dirección")
        String address,

        @Schema(description = "Ciudad")
        String cityName,

        @Schema(description = "Latitud")
        BigDecimal latitude,

        @Schema(description = "Longitud")
        BigDecimal longitude,

        @Schema(description = "Referencia")
        String reference
) {
}

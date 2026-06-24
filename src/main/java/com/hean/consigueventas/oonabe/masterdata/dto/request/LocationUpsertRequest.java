package com.hean.consigueventas.oonabe.masterdata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record LocationUpsertRequest(
        @Schema(example = "Sede Miraflores")
        @NotBlank(message = "El nombre es obligatorio")
        String name,


        @Schema(example = "Av. Larco 123")
        @NotBlank(message = "La dirección es obligatoria")
        String address,


        @Schema(description = "Ciudad.")
        Long cityId,


        @Schema(example = "-12.120000")
        BigDecimal latitude,


        @Schema(example = "-77.030000")
        BigDecimal longitude,


        String reference
) {
}

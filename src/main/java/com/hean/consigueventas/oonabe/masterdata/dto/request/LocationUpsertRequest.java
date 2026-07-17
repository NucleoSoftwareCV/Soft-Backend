package com.hean.consigueventas.oonabe.masterdata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LocationUpsertRequest(
        @Schema(example = "Sede Miraflores")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,


        @Schema(example = "Av. Larco 123")
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255, message = "La direccion no puede superar los 255 caracteres")
        String address,


        @Schema(description = "Ciudad.")
        @NotNull(message = "La ciudad es obligatoria")
        Long cityId,


        @Schema(example = "-12.120000")
        @DecimalMin(value = "-90.0", message = "La latitud no puede ser menor a -90")
        @DecimalMax(value = "90.0", message = "La latitud no puede ser mayor a 90")
        @Digits(integer = 2, fraction = 6, message = "La latitud admite hasta 6 decimales")
        BigDecimal latitude,


        @Schema(example = "-77.030000")
        @DecimalMin(value = "-180.0", message = "La longitud no puede ser menor a -180")
        @DecimalMax(value = "180.0", message = "La longitud no puede ser mayor a 180")
        @Digits(integer = 3, fraction = 6, message = "La longitud admite hasta 6 decimales")
        BigDecimal longitude,


        @Size(max = 1000, message = "La referencia no puede superar los 1000 caracteres")
        String reference
) {
}

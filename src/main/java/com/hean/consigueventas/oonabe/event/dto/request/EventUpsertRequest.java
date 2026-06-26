package com.hean.consigueventas.oonabe.event.dto.request;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos para crear un evento.")
public record EventUpsertRequest(


        @Schema(description = "Título del evento.", example = "Clase de Yoga Inicial")
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 180)
        String title,


        @Schema(description = "Resumen corto del evento.", example = "Aprende técnicas básicas de yoga.")
        @Size(max = 300)
        String summary,


        @Schema(description = "Descripción completa del evento.")
        @NotBlank(message = "La descripción es obligatoria")
        String description,


        @Schema(description = "Modalidad del evento.", example = "VIRTUAL")
        @NotNull(message = "La modalidad es obligatoria")
        EventModality modality,


        @Schema(description = "Precio inicial.", example = "50.00")
        @NotNull(message = "El precio es obligatorio")
        BigDecimal priceFrom,


        @Schema(description = "Moneda.", example = "PEN")
        @NotBlank(message = "La moneda es obligatoria")
        String currency,


        @Schema(description = "Edad mínima permitida.", example = "18")
        Short minimumAge,


        @Schema(description = "Indica si aparece como destacado.")
        Boolean featured,


        @Schema(description = "Categoría del evento.")
        @NotNull(message = "La categoría es obligatoria")
        Long categoryId,

        @Schema(description = "ID del especialista / organizador.", example = "1")
        @NotNull(message = "El ID del especialista es obligatorio")
        Long specialistId
) {
}

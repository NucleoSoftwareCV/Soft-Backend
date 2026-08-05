package com.hean.consigueventas.oonabe.event.dto.request;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventPaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

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

        @Schema(description = "Elementos incluidos en el evento.", example = "[\"Material para practicar yoga\"]")
        @Size(max = 20, message = "No se pueden registrar mas de 20 elementos incluidos")
        List<@NotBlank(message = "El elemento incluido no puede estar vacio") @Size(max = 180) String> includes,


        @Schema(description = "Puntos destacados del evento.", example = "[\"Yoga\", \"Respiracion consciente\"]")
        @Size(max = 20, message = "No se pueden registrar mas de 20 puntos destacados")
        List<@NotBlank(message = "El punto destacado no puede estar vacio") @Size(max = 180) String> highlights,


        @Schema(description = "Elementos que debe traer la persona asistente.", example = "[\"Ropa comoda\"]")
        @Size(max = 20, message = "No se pueden registrar mas de 20 elementos para traer")
        List<@NotBlank(message = "El elemento para traer no puede estar vacio") @Size(max = 180) String> whatToBring,


        @Schema(description = "Modalidad del evento.", example = "ONLINE")
        @NotNull(message = "La modalidad es obligatoria")
        EventModality modality,


        @Schema(description = "Precio inicial.", example = "50.00")
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
        BigDecimal priceFrom,


        @Schema(description = "Moneda.", example = "PEN")
        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "[A-Za-z]{3}", message = "La moneda debe tener exactamente 3 letras")
        String currency,


        @Schema(description = "Edad mínima permitida.", example = "18")
        @Min(value = 0, message = "La edad minima no puede ser negativa")
        @Max(value = 120, message = "La edad minima no puede ser mayor a 120")
        Short minimumAge,


        @Schema(description = "Indica si aparece como destacado.")
        Boolean featured,


        @Schema(description = "Categoría del evento.")
        @NotNull(message = "La categoría es obligatoria")
        Long categoryId,

        @Schema(description = "Método de pago del evento (WHATSAPP o ONLINE).", example = "WHATSAPP")
        EventPaymentMethod paymentMethod,

        @Schema(description = "ID del especialista / organizador.", example = "1")
        @NotNull(message = "El ID del especialista es obligatorio")
        Long specialistId
) {
}

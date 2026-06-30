package com.hean.consigueventas.oonabe.oneToOneSession.dto.request;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un servicio de sesión 1-a-1.")
public record OneToOneServiceRequest(
        @Schema(description = "Título del servicio.", example = "Consulta de Psicoterapia Individual")
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 180, message = "El título no puede superar los 180 caracteres")
        String title,

        @Schema(description = "Descripción detallada del servicio.", example = "Sesión individual de psicoterapia orientada al autoconocimiento...")
        @NotBlank(message = "La descripción es obligatoria")
        String description,

        @Schema(description = "URL de imagen para la tarjeta publica.", example = "https://images.unsplash.com/photo-...")
        String imageUrl,

        @Schema(description = "Duración en minutos.", example = "60")
        @NotNull(message = "La duración es obligatoria")
        Integer durationMinutes,

        @Schema(description = "Modalidad del servicio.", example = "ONLINE")
        @NotNull(message = "La modalidad es obligatoria")
        SessionModality modality,

        @Schema(description = "ID de la ubicación (opcional).", example = "1")
        Long locationId,

        @Schema(description = "Precio del servicio.", example = "50.00")
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0")
        BigDecimal price,

        @Schema(description = "Moneda del precio.", example = "EUR")
        String currency,

        @Schema(description = "Estado de publicación.", example = "BORRADOR")
        PublicationStatus status
) {
}

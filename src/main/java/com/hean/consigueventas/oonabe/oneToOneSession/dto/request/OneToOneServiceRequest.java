package com.hean.consigueventas.oonabe.oneToOneSession.dto.request;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.URL;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import java.math.BigDecimal;
import java.util.Set;

@Schema(description = "Datos para crear o actualizar un servicio de sesion 1-a-1.")
public record OneToOneServiceRequest(
        @Schema(description = "Titulo del servicio.", example = "Consulta de Psicoterapia Individual")
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 180, message = "El titulo no puede superar los 180 caracteres")
        String title,

        @Schema(description = "Descripcion detallada del servicio.", example = "Sesion individual de psicoterapia orientada al autoconocimiento...")
        @NotBlank(message = "La descripcion es obligatoria")
        String description,

        @Schema(description = "URL de imagen para la tarjeta publica.", example = "https://images.unsplash.com/photo-...")
        @SessionImageUrl
        String imageUrl,

        @Schema(description = "Duracion en minutos.", example = "60")
        @NotNull(message = "La duracion es obligatoria")
        @Positive(message = "La duracion debe ser mayor a 0")
        Integer durationMinutes,

        @Schema(description = "Modalidad del servicio.", example = "ONLINE")
        @NotNull(message = "La modalidad es obligatoria")
        SessionModality modality,

        @Schema(description = "ID de la ubicacion opcional.", example = "1")
        Long locationId,

        @Schema(description = "Precio del servicio.", example = "50.00")
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0")
        BigDecimal price,

        @Schema(description = "Moneda del precio. Si viene vacia se usa PEN.", example = "PEN")
        @Pattern(regexp = "^$|[A-Za-z]{3}", message = "La moneda debe tener 3 letras")
        String currency,

        @Schema(description = "Estado de publicacion.", example = "BORRADOR")
        PublicationStatus status,

        @Schema(description = "IDs de los temas de trabajo asociados.")
        Set<Long> workTopics,

        @Schema(description = "IDs de las tecnicas asociadas.")
        Set<Long> techniques
) {

    @ConstraintComposition(CompositionType.OR)
    @URL
    @Pattern(regexp = "(?:/[A-Za-z0-9_-]+)*/[0-9]+/session-cover/session-cover-[0-9a-f-]{36}\\.[A-Za-z0-9]+")
    @ReportAsSingleViolation
    @Constraint(validatedBy = {})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SessionImageUrl {
        String message() default "La URL de imagen no es valida";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}

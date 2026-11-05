package com.hean.consigueventas.oonabe.review.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hean.consigueventas.oonabe.review.support.StarRatingDeserializer;
import jakarta.validation.constraints.*;

public record CreateEventReviewRequest(
        @NotNull(message = "Selecciona una calificación")
        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5")
        @JsonDeserialize(using = StarRatingDeserializer.class)
        Integer stars,
        @Size(max = 2000, message = "El comentario admite hasta 2000 caracteres")
        String comment
) {}

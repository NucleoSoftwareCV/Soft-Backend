package com.hean.consigueventas.oonabe.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Imagen de la galeria de un evento")
public record EventImageResponse(
        @Schema(description = "ID de la imagen")
        Long id,

        @Schema(description = "URL de la imagen")
        String url
) {
}

package com.hean.consigueventas.oonabe.profileProfesional.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Imagen de la galeria publica del profesional")
public record GalleryImageResponse(

        @Schema(description = "ID de la imagen")
        Long id,

        @Schema(description = "URL publica de la imagen")
        String imageUrl,

        @Schema(description = "Orden de aparicion en la galeria")
        Integer sortOrder
) {
}

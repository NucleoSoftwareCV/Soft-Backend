package com.hean.consigueventas.oonabe.profileProfesional.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los datos de una imagen del perfil")
public record ProfessionalImageResponse(

        @Schema(description = "ID de la imagen")
        Long id,

        @Schema(description = "Ruta o URL de la imagen")
        String imageUrl,

        @Schema(description = "Indica si la imagen es el banner")
        boolean banner,

        @Schema(description = "Tamaño del archivo en bytes")
        Integer fileSizeBytes,

        @Schema(description = "Orden de visualización")
        Short displayOrder
) {
}
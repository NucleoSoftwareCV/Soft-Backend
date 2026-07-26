package com.hean.consigueventas.oonabe.profileCliente.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría de interés del cliente")
public record ClientInterestCategoryResponse(

//        @Schema(description = "ID de la categoría")
//        Long id,

        @Schema(description = "Nombre de la categoría")
        String name
) {
}
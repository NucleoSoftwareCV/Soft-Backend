package com.hean.consigueventas.oonabe.masterdata.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmacion de registro de interes por una ciudad.")
public record CityInterestResponse(
        @Schema(example = "Gracias por tu interes. Te avisaremos cuando Oona llegue a tu ciudad.")
        String message
) {
}

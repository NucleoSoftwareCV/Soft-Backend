package com.hean.consigueventas.oonabe.masterdata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Interes de un usuario por una ciudad donde Oona aun no esta disponible.")
public record CityInterestRequest(
        @NotNull(message = "La ciudad es obligatoria")
        @Schema(example = "1")
        Long cityId,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 150, message = "El email debe tener como maximo 150 caracteres")
        @Schema(example = "usuario@email.com")
        String email
) {
}

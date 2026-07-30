package com.hean.consigueventas.oonabe.professionalApplication.dto.request;

import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para solicitar acceso como profesional")
public record ProfessionalApplicationRequest(
        @NotBlank
        @Size(max = 200)
        @Schema(example = "Ana Garcia Lopez")
        String fullName,

        @NotNull
        @Schema(example = "1")
        Long cityId,

        @NotNull
        ProfessionalType professionalType,

        @NotBlank
        @Size(max = 25)
        @Schema(example = "+34600111222")
        String whatsappPhone,

        @NotBlank
        @Size(max = 500)
        @Schema(example = "Soy profesora de yoga y organizo actividades de bienestar.")
        String motivation,

        @AssertTrue(message = "Debes aceptar la politica de privacidad")
        boolean privacyAccepted
) {
}

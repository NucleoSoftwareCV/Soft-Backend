package com.hean.consigueventas.oonabe.community.dto.request;

import com.hean.consigueventas.oonabe.community.enums.MatchAvailableDay;
import com.hean.consigueventas.oonabe.community.enums.MatchDescriptor;
import com.hean.consigueventas.oonabe.community.enums.MatchGender;
import com.hean.consigueventas.oonabe.community.enums.MatchLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Datos de participacion para encontrar personas con intereses compatibles.")
public record MatchRequestUpsertRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(example = "Maria Lopez")
        String name,

        @NotNull
        @Min(18)
        @Max(120)
        @Schema(example = "28", minimum = "18", maximum = "120")
        Integer age,

        @NotBlank
        @Email
        @Size(max = 150)
        @Schema(example = "maria@example.com")
        String email,

        @NotBlank
        @Size(max = 25)
        @Schema(example = "+34 600 000 000")
        String whatsapp,

        @NotBlank
        @Size(max = 255)
        @Schema(example = "Valencia, Sagunto")
        String exactZone,

        @NotNull
        MatchGender gender,

        @NotEmpty
        Set<@NotNull MatchLanguage> languages,

        @NotEmpty
        @Size(max = 3)
        @Schema(description = "IDs de una a tres categorias activas.", example = "[1, 2, 3]")
        Set<@NotNull Long> categoryIds,

        @NotEmpty
        Set<@NotNull MatchAvailableDay> availableDays,

        @NotBlank
        @Size(max = 2000)
        @Schema(example = "Conocer gente para practicar yoga y compartir actividades de bienestar.")
        String expectations,

        @Size(max = 3)
        Set<@NotNull MatchDescriptor> descriptors
) {
}

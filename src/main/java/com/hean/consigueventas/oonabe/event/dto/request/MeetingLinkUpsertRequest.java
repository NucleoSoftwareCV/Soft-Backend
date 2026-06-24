package com.hean.consigueventas.oonabe.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MeetingLinkUpsertRequest(

        @Schema(example = "ZOOM")
        @NotBlank(message = "La plataforma es obligatoria")
        String platform,


        @Schema(example = "https://zoom.us/j/123456")
        @NotBlank(message = "El enlace es obligatorio")
        String meetingUrl,


        @Schema(example = "123456789")
        String meetingId,


        @Schema(example = "1234")
        String password


) {
}

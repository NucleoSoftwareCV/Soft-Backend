package com.hean.consigueventas.oonabe.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record MeetingLinkUpsertRequest(

        @Schema(example = "ZOOM")
        @NotBlank(message = "La plataforma es obligatoria")
        @Size(max = 50, message = "La plataforma no puede superar los 50 caracteres")
        String platform,


        @Schema(example = "https://zoom.us/j/123456")
        @NotBlank(message = "El enlace es obligatorio")
        @URL(message = "El enlace de reunion no es una URL valida")
        String meetingUrl,


        @Schema(example = "123456789")
        @Size(max = 100, message = "El ID de reunion no puede superar los 100 caracteres")
        String meetingId,


        @Schema(example = "1234")
        @Size(max = 100, message = "La clave de reunion no puede superar los 100 caracteres")
        String password


) {
}

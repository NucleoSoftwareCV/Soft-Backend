package com.hean.consigueventas.oonabe.community.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.security.SecurityUtils;
import com.hean.consigueventas.oonabe.community.dto.request.MatchRequestUpsertRequest;
import com.hean.consigueventas.oonabe.community.dto.response.MatchSubmissionResponse;
import com.hean.consigueventas.oonabe.community.service.MatchRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/community")
@Tag(name = "Conocer gente", description = "Inscripcion para recibir un match de bienestar por WhatsApp.")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    public MatchRequestController(MatchRequestService matchRequestService) {
        this.matchRequestService = matchRequestService;
    }

    @PutMapping("/match-request")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Registrar o actualizar participacion",
            description = "Guarda una unica solicitud por usuario para que el equipo realice el match manualmente.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participacion registrada o actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos o categorias invalidas",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuario o categoria no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public MatchSubmissionResponse upsert(@Valid @RequestBody MatchRequestUpsertRequest request) {
        return matchRequestService.upsert(SecurityUtils.getAuthenticatedUserId(), request);
    }
}

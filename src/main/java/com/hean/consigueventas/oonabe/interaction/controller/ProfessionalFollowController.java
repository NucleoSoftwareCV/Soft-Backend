package com.hean.consigueventas.oonabe.interaction.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.interaction.dto.FollowedProfessionalResponse;
import com.hean.consigueventas.oonabe.interaction.dto.ProfessionalFollowStatusResponse;
import com.hean.consigueventas.oonabe.interaction.service.ProfessionalFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/professional-follows")
@Tag(name = "Seguimiento de profesionales", description = "Gestion de profesionales seguidos por el usuario autenticado.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ProfessionalFollowController {

    private final ProfessionalFollowService followService;

    public ProfessionalFollowController(ProfessionalFollowService followService) {
        this.followService = followService;
    }

    @PutMapping("/{professionalId}")
    @Operation(summary = "Seguir profesional", description = "Registra de forma idempotente el seguimiento de un perfil publico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profesional seguido"),
            @ApiResponse(responseCode = "400", description = "No se permite seguir el perfil propio", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Perfil inexistente o no publico", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProfessionalFollowStatusResponse follow(
            Authentication authentication,
            @PathVariable Long professionalId
    ) {
        return followService.follow(authentication.getName(), professionalId);
    }

    @DeleteMapping("/{professionalId}")
    @Operation(summary = "Dejar de seguir profesional", description = "Elimina el seguimiento si existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguimiento eliminado o inexistente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProfessionalFollowStatusResponse unfollow(
            Authentication authentication,
            @PathVariable Long professionalId
    ) {
        return followService.unfollow(authentication.getName(), professionalId);
    }

    @GetMapping("/{professionalId}")
    @Operation(summary = "Consultar seguimiento", description = "Indica si el usuario autenticado sigue al perfil profesional publico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de seguimiento obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Perfil inexistente o no publico", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProfessionalFollowStatusResponse getStatus(
            Authentication authentication,
            @PathVariable Long professionalId
    ) {
        return followService.getStatus(authentication.getName(), professionalId);
    }

    @GetMapping
    @Operation(summary = "Listar profesionales seguidos", description = "Devuelve las cards publicas seguidas, ordenadas por seguimiento reciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado paginado obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public Page<FollowedProfessionalResponse> getFollowedProfessionals(
            Authentication authentication,
            @ParameterObject
            @PageableDefault(size = 12, sort = "followedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return followService.getFollowedProfessionals(authentication.getName(), pageable);
    }

}

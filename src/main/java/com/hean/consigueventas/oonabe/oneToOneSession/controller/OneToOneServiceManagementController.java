package com.hean.consigueventas.oonabe.oneToOneSession.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.security.SecurityConstants;
import com.hean.consigueventas.oonabe.common.security.SecurityUtils;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.request.OneToOneServiceRequest;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.service.OneToOneSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/one-to-one-services")
@Tag(name = "Gestion de sesiones 1-a-1", description = "API privada para gestionar sesiones individuales 1-a-1.")
public class OneToOneServiceManagementController {

    private final OneToOneSessionService service;

    public OneToOneServiceManagementController(OneToOneSessionService service) {
        this.service = service;
    }

    @GetMapping("/my-services")
    @PreAuthorize(SecurityConstants.HAS_ROLE_PROFESSIONAL)
    @Operation(
            summary = "Listar mis sesiones",
            description = "Devuelve todas las sesiones asociadas al especialista logueado.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponse(responseCode = "200", description = "Lista de sesiones del especialista")
    public List<OneToOneServiceResponse> getMyServices() {
        return service.getMyServices(SecurityUtils.getAuthenticatedUserId());
    }

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_PROFESSIONAL)
    @Operation(
            summary = "Crear nueva sesion",
            description = "Crea un nuevo servicio de sesion 1-a-1.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sesion creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<OneToOneServiceResponse> createService(@Valid @RequestBody OneToOneServiceRequest request) {
        OneToOneServiceResponse response = service.createService(SecurityUtils.getAuthenticatedUserId(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_PROFESSIONAL)
    @Operation(
            summary = "Actualizar sesion",
            description = "Modifica los datos de una sesion existente del especialista logueado.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesion actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos o inconsistentes",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Sesion no encontrada o no pertenece al especialista",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "La sesion fue modificada por otra solicitud",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse updateService(@PathVariable Long id, @Valid @RequestBody OneToOneServiceRequest request) {
        return service.updateService(id, SecurityUtils.getAuthenticatedUserId(), request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(SecurityConstants.HAS_ROLE_PROFESSIONAL)
    @Operation(
            summary = "Cambiar estado de publicacion",
            description = "Modifica el estado de una sesion (BORRADOR, PUBLICADO, OCULTO).",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Sesion no encontrada o no pertenece al especialista",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "La sesion fue modificada por otra solicitud",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse toggleStatus(@PathVariable Long id, @RequestParam PublicationStatus status) {
        return service.toggleStatus(id, SecurityUtils.getAuthenticatedUserId(), status);
    }
}

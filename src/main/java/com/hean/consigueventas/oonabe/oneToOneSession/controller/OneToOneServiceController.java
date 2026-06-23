package com.hean.consigueventas.oonabe.oneToOneSession.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/one-to-one-services")
@Tag(name = "Sesiones 1-a-1", description = "Gestión de plantillas de servicios y sesiones individuales 1-a-1.")
public class OneToOneServiceController {

    private final OneToOneSessionService service;

    public OneToOneServiceController(OneToOneSessionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar sesiones publicadas", description = "Devuelve todas las sesiones con estado PUBLICADO.", security = {})
    @ApiResponse(responseCode = "200", description = "Lista de sesiones públicas")
    public List<OneToOneServiceResponse> getPublicServices() {
        return service.getPublicServices();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sesión por ID", description = "Si no está publicada, solo el dueño puede verla.", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión encontrada"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada o no disponible", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse getById(@PathVariable Long id) {
        return service.getById(id, SecurityUtils.getAuthenticatedUserId());
    }

    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Obtener sesión por enlace amigable (nombre legible de la URL)",
            description = "Busca los detalles de una sesión utilizando el texto legible de su enlace en lugar de su número identificador ID. Ejemplo: '/slug/terapia-psicologica' en lugar de buscar por '/5'. Si no está publicada, solo el especialista creador puede verla.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión encontrada"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada o no disponible", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse getBySlug(@PathVariable String slug) {
        return service.getBySlug(slug, SecurityUtils.getAuthenticatedUserId());
    }

    @GetMapping("/my-services")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(summary = "Listar mis sesiones (Especialista)", description = "Devuelve todas las sesiones asociadas al especialista logueado.", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Lista de sesiones del especialista")
    public List<OneToOneServiceResponse> getMyServices() {
        return service.getMyServices(SecurityUtils.getAuthenticatedUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(summary = "Crear nueva sesión (Especialista)", description = "Crea un nuevo servicio de sesión 1-a-1.", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sesión creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse createService(@Valid @RequestBody OneToOneServiceRequest request) {
        return service.createService(SecurityUtils.getAuthenticatedUserId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(summary = "Actualizar sesión (Especialista)", description = "Modifica los datos de una sesión existente del especialista logueado.", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o inconsistentes", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada o no pertenece al especialista", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse updateService(@PathVariable Long id, @Valid @RequestBody OneToOneServiceRequest request) {
        return service.updateService(id, SecurityUtils.getAuthenticatedUserId(), request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    @Operation(summary = "Cambiar estado de publicación", description = "Modifica el estado de una sesión (BORRADOR, PUBLICADO, OCULTO).", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada o no pertenece al especialista", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse toggleStatus(@PathVariable Long id, @RequestParam PublicationStatus status) {
        return service.toggleStatus(id, SecurityUtils.getAuthenticatedUserId(), status);
    }
}

package com.hean.consigueventas.oonabe.oneToOneSession.controller;

import com.hean.consigueventas.oonabe.common.security.SecurityUtils;
import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceCardResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.dto.response.OneToOneServiceResponse;
import com.hean.consigueventas.oonabe.oneToOneSession.service.OneToOneSessionService;
import com.hean.consigueventas.oonabe.oneToOneSession.support.OneToOnePageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/one-to-one-services")
@Tag(name = "Sesiones 1-a-1", description = "API publica para explorar sesiones individuales 1-a-1.")
public class OneToOneServiceController {

    private final OneToOneSessionService service;

    public OneToOneServiceController(OneToOneSessionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Listar sesiones publicadas",
            description = """
                    Devuelve sesiones publicadas y permite buscar por texto o filtrar por tema y tecnica.
                    Parametros de paginacion: page, size y sort.
                    Tamano maximo permitido: 100. Orden por defecto: createdAt,desc.
                    Campos de orden permitidos: createdAt, title, price, durationMinutes.
                    """,
            security = {}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pagina de sesiones publicas con datos minimos para cards.",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
    )
    public PagedResponse<OneToOneServiceCardResponse> getPublicServices(
            @Parameter(description = "Texto de busqueda por titulo, descripcion, especialista, tema o tecnica", example = "psicologia")
            @RequestParam(required = false) String search,
            @Parameter(description = "ID del tema de trabajo", example = "1")
            @RequestParam(required = false) Long workTopicId,
            @Parameter(description = "ID de la tecnica", example = "1")
            @RequestParam(required = false) Long techniqueId,
            @Parameter(description = "ID del especialista", example = "1")
            @RequestParam(required = false) Long specialistId,
            @ParameterObject
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return PagedResponse.from(
                service.getPublicServices(search, workTopicId, techniqueId, specialistId, OneToOnePageables.sanitizePublicListing(pageable))
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener sesion publicada por ID",
            description = "Devuelve el detalle publico de una sesion publicada.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesion encontrada"),
            @ApiResponse(responseCode = "404", description = "Sesion no encontrada o no disponible",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse getById(@PathVariable Long id) {
        return service.getById(id, SecurityUtils.getAuthenticatedUserId());
    }

    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Obtener sesion por enlace amigable",
            description = "Busca los detalles publicos de una sesion utilizando el texto legible de su enlace.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesion encontrada"),
            @ApiResponse(responseCode = "404", description = "Sesion no encontrada o no disponible",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public OneToOneServiceResponse getBySlug(@PathVariable String slug) {
        return service.getBySlug(slug, SecurityUtils.getAuthenticatedUserId());
    }
}

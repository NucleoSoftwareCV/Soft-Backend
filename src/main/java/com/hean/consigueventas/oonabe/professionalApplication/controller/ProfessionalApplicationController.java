package com.hean.consigueventas.oonabe.professionalApplication.controller;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.professionalApplication.dto.request.ProfessionalApplicationDecisionRequest;
import com.hean.consigueventas.oonabe.professionalApplication.dto.request.ProfessionalApplicationRequest;
import com.hean.consigueventas.oonabe.professionalApplication.dto.response.ProfessionalApplicationResponse;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;
import com.hean.consigueventas.oonabe.professionalApplication.service.ProfessionalApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(
        name = "Solicitudes profesionales",
        description = "Solicitud y aprobación de acceso como profesional"
)
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ProfessionalApplicationController {

    private final ProfessionalApplicationService service;

    public ProfessionalApplicationController(
            ProfessionalApplicationService service) {
        this.service = service;
    }

    @PostMapping("/professional-applications")
    @Operation(
            summary = "Enviar solicitud profesional",
            description = "Permite enviar una solicitud profesional a un usuario autenticado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Solicitud enviada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de solicitud inválidos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ProblemDetail.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "El usuario debe iniciar sesión"
            )
    })
    public ProfessionalApplicationResponse createApplication(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @Valid @RequestBody ProfessionalApplicationRequest request) {
        return service.createApplication(
                request,
                principal.getId()
        );
    }

    @GetMapping("/admin/professional-applications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar solicitudes profesionales",
            description = "Permite al administrador consultar las solicitudes"
    )
    public PagedResponse<ProfessionalApplicationResponse> getApplicationsForAdmin(
            @RequestParam(required = false)
            ProfessionalApplicationStatus status,
            @ParameterObject
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {
        return PagedResponse.from(
                service.getApplicationsForAdmin(status, pageable)
        );
    }

    @PatchMapping("/admin/professional-applications/{id}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Aprobar o rechazar solicitud profesional"
    )
    public ProfessionalApplicationResponse decide(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal,
            @Valid @RequestBody ProfessionalApplicationDecisionRequest request) {
        return service.decide(
                id,
                principal.getId(),
                request
        );
    }
}
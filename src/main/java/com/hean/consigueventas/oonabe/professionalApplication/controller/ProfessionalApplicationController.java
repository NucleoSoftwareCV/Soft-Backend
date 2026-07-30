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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Solicitudes profesionales", description = "Solicitud y aprobación de acceso al panel profesional")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ProfessionalApplicationController {

    private final ProfessionalApplicationService service;

    public ProfessionalApplicationController(ProfessionalApplicationService service) {
        this.service = service;
    }

    @PutMapping("/professional-applications/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Guardar mi solicitud profesional")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud guardada"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProfessionalApplicationResponse saveMyApplication(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @Valid @RequestBody ProfessionalApplicationRequest request) {
        return service.saveMyApplication(principal.getId(), request);
    }

    @GetMapping("/professional-applications/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Consultar mi solicitud profesional")
    public ProfessionalApplicationResponse getMyApplication(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return service.getMyApplication(principal.getId());
    }

    @GetMapping("/admin/professional-applications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar solicitudes profesionales")
    public PagedResponse<ProfessionalApplicationResponse> getApplicationsForAdmin(
            @RequestParam(required = false) ProfessionalApplicationStatus status,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return PagedResponse.from(service.getApplicationsForAdmin(status, pageable));
    }

    @PatchMapping("/admin/professional-applications/{id}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar o rechazar una solicitud profesional")
    public ProfessionalApplicationResponse decide(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal,
            @Valid @RequestBody ProfessionalApplicationDecisionRequest request) {
        return service.decide(id, principal.getId(), request);
    }
}

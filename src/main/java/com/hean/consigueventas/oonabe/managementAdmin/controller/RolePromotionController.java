package com.hean.consigueventas.oonabe.managementAdmin.controller;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.managementAdmin.dto.PromotionResponseDto;
import com.hean.consigueventas.oonabe.managementAdmin.dto.RolePromotionRequestDto;
import com.hean.consigueventas.oonabe.managementAdmin.service.RolePromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Role Promotions", description = "Endpoints para solicitudes de ascenso a rol profesional")
public class RolePromotionController {

    private final RolePromotionService rolePromotionService;

    public RolePromotionController(RolePromotionService rolePromotionService) {
        this.rolePromotionService = rolePromotionService;
    }

    @PostMapping("/customers/promote")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Solicitar ascenso", description = "Permite a un cliente solicitar ser profesional.")
    public PromotionResponseDto createPromotionRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody RolePromotionRequestDto requestDto) {
        return rolePromotionService.createPromotionRequest(userDetails.getId(), requestDto);
    }

    @PutMapping("/admin/promotions/{id}/evaluate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Evaluar solicitud", description = "Permite a un admin aprobar o rechazar una solicitud.")
    public PromotionResponseDto evaluatePromotionRequest(
            @PathVariable Long id,
            @RequestParam String status) {
        return rolePromotionService.evaluatePromotionRequest(id, status);
    }
}

package com.hean.consigueventas.oonabe.campaign.controller;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignEntryStatus;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.AdminEntry;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.AdminResponse;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.EventsRequest;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.StatusRequest;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.UpsertRequest;
import com.hean.consigueventas.oonabe.campaign.service.PromoCampaignService;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/campaigns")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Campañas (admin)", description = "Configuración de campañas promocionales, experiencias incluidas y participaciones")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminPromoCampaignController {

    private final PromoCampaignService campaignService;

    public AdminPromoCampaignController(PromoCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    @Operation(summary = "Listar campañas")
    public List<AdminResponse> list() {
        return campaignService.adminList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener campaña")
    public AdminResponse get(@PathVariable Long id) {
        return campaignService.adminGet(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear campaña (queda en BORRADOR)")
    public AdminResponse create(@Valid @RequestBody UpsertRequest request) {
        return campaignService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar campaña")
    public AdminResponse update(@PathVariable Long id, @Valid @RequestBody UpsertRequest request) {
        return campaignService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar el estado (BORRADOR, PUBLICADA, FINALIZADA)")
    public AdminResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return campaignService.updateStatus(id, request.status());
    }

    @PutMapping("/{id}/events")
    @Operation(summary = "Reemplazar las experiencias incluidas")
    public AdminResponse replaceEvents(@PathVariable Long id, @Valid @RequestBody EventsRequest request) {
        return campaignService.replaceEvents(id, request.eventIds());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar campaña", description = "Solo campañas en BORRADOR sin participaciones.")
    public void delete(@PathVariable Long id) {
        campaignService.delete(id);
    }

    @PostMapping("/{id}/draw")
    @Operation(
            summary = "Realizar el sorteo",
            description = "Elige una participación válida al azar, ponderada por papeletas, y finaliza la campaña. "
                    + "Requiere que la campaña esté publicada y que su periodo de participación haya cerrado.")
    public AdminResponse draw(@PathVariable Long id) {
        return campaignService.draw(id, SecurityUtils.getAuthenticatedUserId());
    }

    @GetMapping("/{id}/entries")
    @Operation(summary = "Listar participaciones", description = "Una fila por reserva; ticketCount son las plazas que aporta.")
    public List<AdminEntry> entries(@PathVariable Long id, @RequestParam(required = false) CampaignEntryStatus status) {
        return campaignService.adminEntries(id, status);
    }
}

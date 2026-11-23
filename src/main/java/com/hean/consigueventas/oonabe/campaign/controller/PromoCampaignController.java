package com.hean.consigueventas.oonabe.campaign.controller;

import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.Detail;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.MyEntries;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.Summary;
import com.hean.consigueventas.oonabe.campaign.service.CampaignEntryService;
import com.hean.consigueventas.oonabe.campaign.service.PromoCampaignService;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
@Tag(name = "Campañas", description = "Campañas promocionales (sorteos) visibles en Explorar")
public class PromoCampaignController {

    private final PromoCampaignService campaignService;
    private final CampaignEntryService entryService;

    public PromoCampaignController(PromoCampaignService campaignService, CampaignEntryService entryService) {
        this.campaignService = campaignService;
        this.entryService = entryService;
    }

    @GetMapping("/active")
    @Operation(
            summary = "Listar campañas visibles",
            description = "Campañas publicadas que todavía no han cerrado, incluidas las próximas. "
                    + "Los eventos participantes se consultan con GET /api/v1/events?campaignSlug={slug}.",
            security = {})
    @ApiResponse(responseCode = "200", description = "Campañas visibles")
    public List<Summary> active() {
        return campaignService.visibleCampaigns();
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Detalle público de una campaña", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de la campaña"),
            @ApiResponse(responseCode = "404", description = "Campaña inexistente o en borrador",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public Detail detail(@PathVariable String slug) {
        return campaignService.publicDetail(slug);
    }

    @GetMapping("/{slug}/entries/me")
    @Operation(
            summary = "Mis participaciones en la campaña",
            description = "Las participaciones se registran automáticamente al confirmarse una reserva elegible.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participaciones del cliente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Campaña inexistente o en borrador",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public MyEntries myEntries(@PathVariable String slug) {
        return entryService.myEntries(slug, SecurityUtils.getAuthenticatedUserId());
    }
}

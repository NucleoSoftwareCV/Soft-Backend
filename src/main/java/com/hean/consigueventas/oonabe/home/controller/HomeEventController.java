package com.hean.consigueventas.oonabe.home.controller;

import com.hean.consigueventas.oonabe.home.dto.response.HomeEventSectionsResponse;
import com.hean.consigueventas.oonabe.home.service.HomeEventSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@Tag(name = "Home", description = "Composicion publica de la pagina principal")
@Validated
public class HomeEventController {

    private final HomeEventSectionService homeEventSectionService;

    public HomeEventController(HomeEventSectionService homeEventSectionService) {
        this.homeEventSectionService = homeEventSectionService;
    }

    @GetMapping("/event-sections")
    @Operation(
            summary = "Obtener secciones de eventos de la home",
            description = """
                    Devuelve carruseles publicos con eventos proximos para la ciudad seleccionada.
                    Incluye eventos presenciales de esa ciudad y eventos online.
                    Las secciones vacias se omiten.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secciones obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Ciudad o limite invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<HomeEventSectionsResponse> getEventSections(
            @Parameter(description = "Ciudad para los eventos presenciales", example = "Valencia")
            @RequestParam(defaultValue = "Valencia")
            @NotBlank
            @Size(max = 100)
            String cityName,

            @Parameter(description = "Cantidad maxima de tarjetas por seccion", example = "4")
            @RequestParam(defaultValue = "4")
            @Min(1)
            @Max(12)
            int limit) {
        return ResponseEntity.ok(homeEventSectionService.getEventSections(cityName.trim(), limit));
    }
}

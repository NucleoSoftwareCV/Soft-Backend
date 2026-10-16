package com.hean.consigueventas.oonabe.recommendation.controller;

import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.recommendation.EventRecommendationQueryService;
import com.hean.consigueventas.oonabe.recommendation.dto.response.RecommendedEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Recomendaciones",
        description = "Eventos recomendados para el cliente autenticado"
)
public class RecommendationController {

    private final EventRecommendationQueryService queryService;

    @GetMapping("/events")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Consultar eventos recomendados",
            description = """
                    Devuelve eventos ordenados por puntaje descendente,
                    fecha elegible ascendente e identificador.
                    Las paginas comienzan en cero.
                    El tamano maximo de pagina es 100.
                    No admite un orden externo mediante sort.
                    """
    )
    public ResponseEntity<PagedResponse<RecommendedEventResponse>> getEvents(
            Authentication authentication,

            @RequestParam(name = "page", defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(name = "size", defaultValue = "12")
            @Min(1)
            @Max(100)
            int size,

            @RequestParam(name = "sort", required = false)
            String sort
    ) {
        if (sort != null) {
            throw new IllegalArgumentException(
                    "Las recomendaciones tienen un orden fijo"
            );
        }

        PagedResponse<RecommendedEventResponse> response =
                PagedResponse.from(
                        queryService.getRecommendations(
                                authentication.getName(),
                                PageRequest.of(page, size)
                        )
                );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().cachePrivate())
                .body(response);
    }
}
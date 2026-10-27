package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventMapPointResponse;
import com.hean.consigueventas.oonabe.event.repository.EventMapRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;

@RestController
@Validated
@Tag(name = "Eventos")
public class EventMapController {
    private final EventMapRepository repository;
    private final Clock clock;

    public EventMapController(EventMapRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @GetMapping("/api/v1/events/map")
    @Operation(
            summary = "Ubicaciones públicas de sesiones futuras disponibles",
            description = "Excluye eventos online y ubicaciones sin coordenadas válidas. Orden estable por fecha e ID de sesión.",
            security = {}
    )
    public ResponseEntity<PagedResponse<EventMapPointResponse>> getMapPoints(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "100") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(PagedResponse.from(repository.findAvailableLocations(
                        clock.instant(), PageRequest.of(page, size)
                )));
    }
}

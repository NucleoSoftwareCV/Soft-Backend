package com.hean.consigueventas.oonabe.location.controller;

import com.hean.consigueventas.oonabe.location.dto.LocationDTO;
import com.hean.consigueventas.oonabe.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubicaciones")
@Tag(name = "Ubicaciones", description = "Ubicaciones fisicas activas para eventos y sesiones.")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "Listar ubicaciones activas", description = "Devuelve solo ubicaciones publicas y activas.", security = {})
    @ApiResponse(responseCode = "200", description = "Ubicaciones activas")
    public List<LocationDTO> findActive() {
        return locationService.findActive();
    }
}

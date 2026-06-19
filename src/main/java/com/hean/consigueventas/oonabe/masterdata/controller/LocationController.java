package com.hean.consigueventas.oonabe.masterdata.controller;

import com.hean.consigueventas.oonabe.location.dto.response.LocationResponse;
import com.hean.consigueventas.oonabe.masterdata.dto.LocationDTO;
import com.hean.consigueventas.oonabe.masterdata.service.ILocationService;
import com.hean.consigueventas.oonabe.masterdata.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Tag(name = "Ubicaciones", description = "Ubicaciones físicas activas para eventos y sesiónes.")
public class LocationController {
    private final ILocationService locationService;

    public LocationController(ILocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "Listar ubicaciones activas", description = "Devuelve solo ubicaciones públicas y activas.", security = {})
    @ApiResponse(responseCode = "200", description = "Ubicaciones activas")
    public List<LocationResponse> findActive() {
        return locationService.findActive();
    }
}

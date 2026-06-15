package com.hean.consigueventas.oonabe.location.controller;

import com.hean.consigueventas.oonabe.location.dto.LocationDTO;
import com.hean.consigueventas.oonabe.location.service.LocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubicaciones")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<LocationDTO> findActive() {
        return locationService.findActive();
    }
}

package com.hean.consigueventas.oonabe.masterdata.controller;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.CityAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.CityPublicDTO;
import com.hean.consigueventas.oonabe.masterdata.service.CityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")

public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    // Endpoint público
    @GetMapping
    public List<CityPublicDTO> findActive() {
        return cityService.findActive();
    }

    // Endpoint administrativo
    @GetMapping("/admin")
    public List<CityAdminDTO> findAllForAdmin(
            @RequestParam(required = false) Boolean active
    ) {
        return cityService.findAllForAdmin(active);
    }
}
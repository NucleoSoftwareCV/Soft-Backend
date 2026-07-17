package com.hean.consigueventas.oonabe.profileProfesional.controller;

import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.SpecialistProfileResponse;
import com.hean.consigueventas.oonabe.profileProfesional.service.SpecialistProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/specialist-profiles")
public class SpecialistProfileController {

    private final SpecialistProfileService specialistProfileService;

    public SpecialistProfileController(
            SpecialistProfileService specialistProfileService
    ) {
        this.specialistProfileService = specialistProfileService;
    }

    //Profesional: crear perfil
    @PostMapping
    @PreAuthorize("hasRole('PROFESIONAL')")
    public SpecialistProfileResponse createProfile(
            Authentication authentication,
            @Valid @RequestBody SpecialistProfileRequest request
    ) {
        return specialistProfileService.createProfile(
                authentication.getName(),
                request
        );
    }

    //Profesional: ver su perfil
    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public SpecialistProfileResponse getMyProfile(
            Authentication authentication
    ) {
        return specialistProfileService.getMyProfile(
                authentication.getName()
        );
    }
}
package com.hean.consigueventas.oonabe.profileProfesional.controller;

import com.hean.consigueventas.oonabe.profileProfesional.dto.response.ProfessionalDirectoryResponse;
import com.hean.consigueventas.oonabe.profileProfesional.service.ProfessionalDirectoryService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
public class ProfessionalDirectoryController {
    private final ProfessionalDirectoryService service;

    public ProfessionalDirectoryController(ProfessionalDirectoryService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/specialist-profiles/directory")
    public ResponseEntity<ProfessionalDirectoryResponse> directory(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String location,
            @RequestParam(defaultValue = "professionals") String type,
            @RequestParam(defaultValue = "") String specialty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 50 || search.length() > 150
                || location.length() > 100 || specialty.length() > 150
                || !Set.of("professionals", "centers").contains(type)) {
            throw new ResponseStatusException(HttpStatus.   BAD_REQUEST, "Filtros o paginación no válidos");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
                service.get(search.trim(), location.trim(), type, specialty.trim(), page, size));
    }
}

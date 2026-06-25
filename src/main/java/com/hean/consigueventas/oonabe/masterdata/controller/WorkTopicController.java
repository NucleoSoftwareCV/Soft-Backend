package com.hean.consigueventas.oonabe.masterdata.controller;

import com.hean.consigueventas.oonabe.masterdata.dto.WorkTopicDTO;
import com.hean.consigueventas.oonabe.masterdata.service.WorkTopicService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/work-topics")
public class WorkTopicController {

    private final WorkTopicService workTopicService;

    public WorkTopicController(WorkTopicService workTopicService) {
        this.workTopicService = workTopicService;
    }

    // Solo el ADMIN puede ver los temas activos e inactivos
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<WorkTopicDTO> getAllTopics() {
        return workTopicService.getAllTopics();
    }

    // Todos pueden ver los temas activos
    @GetMapping("/active")
    public List<WorkTopicDTO> getActiveTopics() {
        return workTopicService.getActiveTopics();
    }

    // Solo el ADMIN puede buscar un tema por su nombre
    @GetMapping("/search")
    public WorkTopicDTO getTopicByName(
            @RequestParam String name
    ) {
        return workTopicService.getTopicByName(name);
    }

    // Solo el ADMIN puede crear un tema
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public WorkTopicDTO createTopic(
            @Valid @RequestBody WorkTopicDTO workTopicDTO
    ) {
        return workTopicService.createTopic(workTopicDTO);
    }

    // Solo el ADMIN puede modificar un tema
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public WorkTopicDTO updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody WorkTopicDTO workTopicDTO
    ) {
        return workTopicService.updateTopic(id, workTopicDTO);
    }

    // Solo el ADMIN puede desactivar un tema
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateTopic(@PathVariable Long id) {
        workTopicService.deactivateTopic(id);
    }
}
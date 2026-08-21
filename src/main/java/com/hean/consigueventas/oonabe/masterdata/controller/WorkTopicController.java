package com.hean.consigueventas.oonabe.masterdata.controller;

import com.hean.consigueventas.oonabe.masterdata.dto.Admin.WorkTopicAdminDTO;
import com.hean.consigueventas.oonabe.masterdata.dto.User.WorkTopicPublicDTO;
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

    // Admin: ve todos o filtra por estado
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<WorkTopicAdminDTO> getAllTopics(
            @RequestParam(required = false) Boolean active
    ) {
        return workTopicService.getAllTopics(active);
    }

    // Público: ve únicamente temas activos
    @GetMapping("/active")
    public List<WorkTopicPublicDTO> getActiveTopics() {
        return workTopicService.getActiveTopics();
    }

    // Público: busca únicamente temas activos por nombre
    @GetMapping("/search")
    public WorkTopicPublicDTO getTopicByName(
            @RequestParam String name
    ) {
        return workTopicService.getTopicByName(name);
    }

    // Admin: crea un tema
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public WorkTopicAdminDTO createTopic(
            @Valid @RequestBody WorkTopicAdminDTO workTopicAdminDTO
    ) {
        return workTopicService.createTopic(workTopicAdminDTO);
    }

    // Admin: actualiza un tema
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public WorkTopicAdminDTO updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody WorkTopicAdminDTO workTopicAdminDTO
    ) {
        return workTopicService.updateTopic(
                id,
                workTopicAdminDTO
        );
    }

    // Admin: desactiva un tema
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateTopic(@PathVariable Long id) {
        workTopicService.deactivateTopic(id);
    }
}
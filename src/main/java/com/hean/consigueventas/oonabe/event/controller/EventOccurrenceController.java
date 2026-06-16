package com.hean.consigueventas.oonabe.event.controller;

import com.hean.consigueventas.oonabe.event.dto.EventOccurrenceAdminDTO;
import com.hean.consigueventas.oonabe.event.dto.EventOccurrencePublicDTO;
import com.hean.consigueventas.oonabe.event.dto.EventOccurrenceUpsertDTO;
import com.hean.consigueventas.oonabe.event.service.EventOccurrenceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/event-occurrences")
public class EventOccurrenceController {

    private final EventOccurrenceService occurrenceService;

    public EventOccurrenceController(EventOccurrenceService occurrenceService) {
        this.occurrenceService = occurrenceService;
    }

    @GetMapping
    public List<EventOccurrenceAdminDTO> getAllOccurrences() {
        return occurrenceService.getAllOccurrences();
    }

    @GetMapping("/public")
    public List<EventOccurrencePublicDTO> getPublicOccurrences() {
        return occurrenceService.getPublicOccurrences();
    }

    @GetMapping("/public/date")
    public List<EventOccurrencePublicDTO> getPublicOccurrencesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return occurrenceService.getPublicOccurrencesByDate(date);
    }

    @GetMapping("/public/range")
    public List<EventOccurrencePublicDTO> getPublicOccurrencesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return occurrenceService.getPublicOccurrencesByDateRange(startDate, endDate);
    }

    @GetMapping("/event/{eventId}")
    public List<EventOccurrenceAdminDTO> getOccurrencesByEvent(@PathVariable Long eventId) {
        return occurrenceService.getOccurrencesByEvent(eventId);
    }

    @GetMapping("/{id}")
    public EventOccurrenceAdminDTO getOccurrenceById(@PathVariable Long id) {
        return occurrenceService.getOccurrenceById(id);
    }

    @GetMapping("/filter")
    public List<EventOccurrenceAdminDTO> filterOccurrences(
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate selectedDate
    ) {
        return occurrenceService.filterOccurrences(dateFilter, timeFilter, selectedDate);
    }

    @PostMapping
    public EventOccurrenceAdminDTO createOccurrence(@Valid @RequestBody EventOccurrenceUpsertDTO dto) {
        return occurrenceService.createOccurrence(dto);
    }

    @PutMapping("/{id}")
    public EventOccurrenceAdminDTO updateOccurrence(
            @PathVariable Long id,
            @Valid @RequestBody EventOccurrenceUpsertDTO dto
    ) {
        return occurrenceService.updateOccurrence(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteOccurrence(@PathVariable Long id) {
        occurrenceService.deleteOccurrence(id);
    }
}

package com.hean.consigueventas.oonabe.support.controller;

import com.hean.consigueventas.oonabe.support.dto.SupportDtos.*;
import com.hean.consigueventas.oonabe.support.enums.*;
import com.hean.consigueventas.oonabe.support.service.*;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/support")
public class SupportTicketController {
    private final SupportTicketService tickets;
    private final SupportAccess access;

    @GetMapping("/references/my")
    public List<Map<String, Object>> references() {
        return tickets.references();
    }

    @PostMapping("/tickets")
    public Map<String, Object> create(@Valid @RequestBody TicketInput in) {
        return tickets.create(in);
    }

    @GetMapping("/tickets/my")
    public Page<Map<String, Object>> my(@RequestParam(defaultValue = "0") int page) {
        return tickets.my(page);
    }

    @GetMapping("/tickets/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return tickets.detail(id);
    }

    @PostMapping("/tickets/{id}/reopen")
    public Map<String, Object> reopen(
            @PathVariable Long id, @Valid @RequestBody ApplicationInput in) {
        return tickets.reopen(id, in.reason());
    }

    @GetMapping("/admin/tickets")
    public Page<Map<String, Object>> all(
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(defaultValue = "false") boolean complaints,
            @RequestParam(defaultValue = "0") int page) {
        return tickets.all(category, status, priority, complaints, page);
    }

    @GetMapping("/admin/tickets/{id}")
    public Map<String, Object> staffDetail(@PathVariable Long id) {
        access.admin();
        return tickets.detail(id);
    }

    @GetMapping("/admin/dashboard")
    public Map<String, Object> dashboard() {
        return tickets.dashboard();
    }

    @PatchMapping("/admin/tickets/{id}/status")
    public Map<String, Object> status(@PathVariable Long id, @Valid @RequestBody StatusInput in) {
        return tickets.status(id, in);
    }

    @PatchMapping("/admin/tickets/{id}/priority")
    public Map<String, Object> priority(@PathVariable Long id, @Valid @RequestBody PriorityInput in) {
        return tickets.priority(id, in);
    }
}

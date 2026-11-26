package com.hean.consigueventas.oonabe.support.controller;

import com.hean.consigueventas.oonabe.support.service.*;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import com.hean.consigueventas.oonabe.support.dto.SupportDtos.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/support")
public class RefundRequestController {
    private final RefundRequestService refunds;
    private final SupportAccess access;

    @PostMapping("/refunds")
    public Map<String, Object> create(@Valid @RequestBody RefundInput in) {
        return refunds.create(in);
    }

    @GetMapping("/refunds/my")
    public Page<Map<String, Object>> my(@RequestParam(defaultValue = "0") int page) {
        return refunds.my(page);
    }

    @GetMapping("/refunds/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return refunds.detail(id);
    }

    @GetMapping("/operations/my")
    public List<Map<String, Object>> operations() {
        return refunds.operations();
    }

    @GetMapping("/admin/refunds")
    public Page<Map<String, Object>> all(@RequestParam(defaultValue = "0") int page) {
        return refunds.all(page);
    }

    @GetMapping("/admin/refunds/{id}")
    public Map<String, Object> staffDetail(@PathVariable Long id) {
        access.admin();
        return refunds.detail(id);
    }

    @PatchMapping("/admin/refunds/{id}/evaluate")
    public Map<String, Object> evaluate(@PathVariable Long id) {
        return refunds.evaluate(id);
    }

    @PatchMapping("/admin/refunds/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id, @Valid @RequestBody DecisionInput in) {
        return refunds.decide(id, in, true);
    }

    @PatchMapping("/admin/refunds/{id}/reject")
    public Map<String, Object> reject(@PathVariable Long id, @Valid @RequestBody DecisionInput in) {
        return refunds.decide(id, in, false);
    }
}

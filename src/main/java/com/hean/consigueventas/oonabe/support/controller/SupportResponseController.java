package com.hean.consigueventas.oonabe.support.controller;

import com.hean.consigueventas.oonabe.support.service.*;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.hean.consigueventas.oonabe.support.dto.SupportDtos.ResponseInput;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/support")
public class SupportResponseController {
    private final SupportTicketService tickets;
    private final SupportAttachmentService attachments;

    @PostMapping("/tickets/{id}/responses")
    public Map<String, Object> respond(@PathVariable Long id, @Valid @RequestBody ResponseInput in) {
        return tickets.respond(id, in, false);
    }

    @PostMapping("/admin/tickets/{id}/responses")
    public Map<String, Object> staffRespond(
            @PathVariable Long id, @Valid @RequestBody ResponseInput in) {
        return tickets.respond(id, in, true);
    }

    @PostMapping(value = "/tickets/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Long responseId)
            throws IOException {
        return attachments.upload(id, file, responseId);
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        var a = attachments.metadata(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(a.getFileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(attachments.resource(a));
    }
}

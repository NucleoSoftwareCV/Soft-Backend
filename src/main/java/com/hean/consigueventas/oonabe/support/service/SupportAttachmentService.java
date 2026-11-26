package com.hean.consigueventas.oonabe.support.service;

import com.hean.consigueventas.oonabe.support.entity.*;
import com.hean.consigueventas.oonabe.support.mapper.SupportMapper;
import com.hean.consigueventas.oonabe.support.repository.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportAttachmentService {
    private final SupportTicketService tickets;
    private final SupportAccess access;
    private final SupportAttachmentRepository attachments;
    private final SupportMapper mapper;

    @Value("${app.support.storage-path:./private-storage/support}")
    private String directory;

    private static final int MAX = 5 * 1024 * 1024;

    public Map<String, Object> upload(Long id, MultipartFile file, Long responseId)
            throws IOException {
        var t = tickets.read(id);
        tickets.lock(id);
        if (!t.getUser().getId().equals(access.user().getId())) access.admin();
        if (t.getStatus() == com.hean.consigueventas.oonabe.support.enums.TicketStatus.CERRADO
                || t.getStatus() == com.hean.consigueventas.oonabe.support.enums.TicketStatus.RESUELTO)
            throw SupportTicketService.bad("Reabre el ticket para adjuntar evidencia");
        if (file.isEmpty() || file.getSize() > MAX)
            throw SupportTicketService.bad("Máximo 5 MB por archivo");
        byte[] data;
        try (var in = file.getInputStream()) {
            data = in.readNBytes(MAX + 1);
        }
        if (data.length > MAX) throw SupportTicketService.bad("Archivo demasiado grande");
        String name =
                Optional.ofNullable(file.getOriginalFilename())
                        .orElse("evidencia")
                        .replace('\\', '_')
                        .replace('/', '_')
                        .replaceAll("[\\p{Cntrl}]", "_");
        if (name.length() > 255) throw SupportTicketService.bad("Nombre de archivo demasiado largo");
        String lower = name.toLowerCase(Locale.ROOT);
        String mime;
        if (lower.endsWith(".png")
                && data.length > 8
                && Arrays.equals(
                Arrays.copyOf(data, 8), new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10}))
            mime = "image/png";
        else if ((lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
                && data.length > 3
                && (data[0] & 255) == 255
                && (data[1] & 255) == 216
                && (data[2] & 255) == 255) mime = "image/jpeg";
        else if (lower.endsWith(".pdf")
                && data.length > 5
                && new String(data, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))
            mime = "application/pdf";
        else throw SupportTicketService.bad("Solo PDF, PNG o JPG con firma válida");
        if (!mime.equals(file.getContentType()))
            throw SupportTicketService.bad("El tipo MIME no coincide con el contenido");
        if (attachments.findByTicketIdOrderByCreatedAtAsc(id).size() >= 20)
            throw SupportTicketService.bad("Máximo 20 evidencias por ticket");
        // responseId no se acepta sin validar pertenencia y visibilidad.
        if (responseId != null)
            throw SupportTicketService.bad("Adjunta la evidencia al ticket sin responseId");
        Path dir = Path.of(directory).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        String key = UUID.randomUUID().toString();
        Path path = dir.resolve(key);
        Files.write(path, data, StandardOpenOption.CREATE_NEW);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED)
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                            }
                    }
                });
        var a = new SupportAttachment();
        a.setTicket(t);
        a.setUploadedBy(access.user());
        a.setFileName(name);
        a.setContentType(mime);
        a.setFilePath(key);
        attachments.save(a);
        tickets.audit(t, "EVIDENCIA", "Se adjuntó " + name, false);
        return mapper.attachment(a);
    }

    public SupportAttachment metadata(Long id) {
        var a = attachments.findById(id).orElseThrow(SupportTicketService::missing);
        tickets.read(a.getTicket().getId());
        if (a.getResponse() != null && a.getResponse().isInternal() && !access.canReadAll())
            throw new org.springframework.security.access.AccessDeniedException("Evidencia privada");
        return a;
    }

    public Resource resource(SupportAttachment a) throws IOException {
        Path dir = Path.of(directory).toAbsolutePath().normalize();
        Path path = dir.resolve(a.getFilePath()).normalize();
        if (!path.startsWith(dir) || !Files.isRegularFile(path)) throw SupportTicketService.missing();
        return new FileSystemResource(path);
    }
}

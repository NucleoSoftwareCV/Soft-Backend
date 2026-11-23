package com.hean.consigueventas.oonabe.notification.controller;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.notification.dto.NotificationDtos.Item;
import com.hean.consigueventas.oonabe.notification.dto.NotificationDtos.UnreadCount;
import com.hean.consigueventas.oonabe.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Bandeja de avisos automáticos de reservas del usuario autenticado")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class NotificationController {
    private final NotificationService service;

    @GetMapping("/me")
    @Operation(summary = "Listar mis notificaciones, de la más reciente a la más antigua")
    public PagedResponse<Item> list(@AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(user.getId(), page, size);
    }

    @GetMapping("/me/unread-count")
    @Operation(summary = "Contar mis notificaciones sin leer")
    public UnreadCount unreadCount(@AuthenticationPrincipal UserDetailsImpl user) {
        return new UnreadCount(service.unreadCount(user.getId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar una notificación como leída")
    public Item markRead(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable Long id) {
        return service.markRead(user.getId(), id);
    }

    @PatchMapping("/me/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marcar todas mis notificaciones como leídas")
    public void markAllRead(@AuthenticationPrincipal UserDetailsImpl user) {
        service.markAllRead(user.getId());
    }
}

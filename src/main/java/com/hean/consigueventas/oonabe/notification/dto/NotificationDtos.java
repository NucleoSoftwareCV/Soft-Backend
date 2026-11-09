package com.hean.consigueventas.oonabe.notification.dto;

import com.hean.consigueventas.oonabe.notification.domain.NotificationType;

import java.time.Instant;

public final class NotificationDtos {
    private NotificationDtos() {}

    public record Item(Long id, NotificationType type, String title, String message, String eventTitle,
            String bookingCode, String actionLabel, boolean read, Instant createdAt) {}

    public record UnreadCount(long count) {}
}

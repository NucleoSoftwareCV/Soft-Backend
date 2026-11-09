package com.hean.consigueventas.oonabe.notification.service;

import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.common.dto.response.PagedResponse;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.notification.domain.NotificationType;
import com.hean.consigueventas.oonabe.notification.dto.NotificationDtos.Item;
import com.hean.consigueventas.oonabe.notification.entity.Notification;
import com.hean.consigueventas.oonabe.notification.repository.NotificationRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notifications;
    private final Clock clock;

    /** Se ejecuta en la transacción del cambio de estado: el aviso existe solo si el cambio se guarda. */
    @Transactional
    public void notifyCustomer(EventBooking booking, NotificationType type) {
        save(booking.getCustomer().getUser(), booking, type);
    }

    @Transactional
    public void notifyProfessional(EventBooking booking, NotificationType type) {
        save(booking.getOccurrence().getEvent().getSpecialist().getUser(), booking, type);
    }

    public PagedResponse<Item> list(Long userId, int page, int size) {
        return PagedResponse.from(notifications.findByUserId(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"))).map(this::item));
    }

    public long unreadCount(Long userId) {
        return notifications.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public Item markRead(Long userId, Long id) {
        Notification notification = notifications.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada."));
        if (notification.getReadAt() == null) {
            notification.setReadAt(clock.instant());
        }
        return item(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notifications.markAllRead(userId, clock.instant());
    }

    private void save(User recipient, EventBooking booking, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setType(type);
        notification.setTitle(type.getTitle());
        notification.setMessage(type.getMessage());
        notification.setEventTitle(booking.getOccurrence().getEvent().getTitle());
        notification.setBookingCode(booking.getCode());
        notification.setActionLabel(type.getActionLabel());
        notification.setCreatedAt(clock.instant());
        notifications.save(notification);
    }

    private Item item(Notification n) {
        return new Item(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getEventTitle(),
                n.getBookingCode(), n.getActionLabel(), n.getReadAt() != null, n.getCreatedAt());
    }
}

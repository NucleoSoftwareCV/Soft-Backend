package com.hean.consigueventas.oonabe.review.service;

import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.review.dto.request.CreateEventReviewRequest;
import com.hean.consigueventas.oonabe.review.dto.response.*;
import com.hean.consigueventas.oonabe.review.entity.EventReview;
import com.hean.consigueventas.oonabe.review.repository.EventReviewRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Clock;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class EventReviewService {
    private final EventReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventBookingRepository bookingRepository;
    private final Clock clock;

    public EventReviewService(EventReviewRepository reviewRepository, EventRepository eventRepository,
                              UserRepository userRepository, EventBookingRepository bookingRepository, Clock clock) {
        this.reviewRepository = reviewRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    public EventRatingSummaryResponse getSummary(Long eventId) {
        publicEvent(eventId);
        return summaries(List.of(eventId)).get(eventId);
    }

    // Dos consultas por lote: estadísticas y hasta cuatro avatares por evento.
    public Map<Long, EventRatingSummaryResponse> summaries(Collection<Long> eventIds) {
        Map<Long, EventRatingSummaryResponse> result = new HashMap<>();
        eventIds.forEach(id -> result.put(id, EventRatingSummaryResponse.empty(id)));
        if (!eventIds.isEmpty()) {
            Map<Long, List<EventReviewerPreviewResponse>> previews = new HashMap<>();
            reviewRepository.findReviewerPreviews(eventIds).forEach(author -> {
                String name = publicName(author.getAuthorName());
                previews.computeIfAbsent(author.getEventId(), id -> new ArrayList<>())
                        .add(new EventReviewerPreviewResponse(name, author.getAvatarUrl()));
            });
            reviewRepository.summarizeByEventIds(eventIds).forEach(totals -> result.put(
                    totals.getEventId(), new EventRatingSummaryResponse(totals.getEventId(),
                            totals.getAverageRating(), totals.getReviewCount(),
                            List.copyOf(previews.getOrDefault(totals.getEventId(), List.of())))));
        }
        return result;
    }

    public Page<EventReviewResponse> getReviews(Long eventId, int page, int size) {
        publicEvent(eventId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return reviewRepository.findByEventId(eventId, pageable).map(this::toResponse);
    }

    public EventReviewEligibilityResponse getEligibility(Long eventId, Long userId) {
        Event event = publicEvent(eventId);
        User user = authenticatedUser(userId);
        return eligibility(event, user);
    }

    @Transactional
    public EventReviewResponse createReview(Long eventId, Long userId, CreateEventReviewRequest request) {
        Event event = publicEvent(eventId);
        User user = authenticatedUser(userId);
        // También se comprueba aquí, aunque Angular haya consultado el permiso.
        if (reviewRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya registraste una reseña para este evento.");
        }
        EventReviewEligibilityResponse permission = eligibility(event, user);
        if (!permission.canReview()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, permission.reason());
        }
        EventReview review = new EventReview();
        review.setEvent(event);
        review.setUser(user);
        review.setStars(request.stars());
        String comment = request.comment() == null ? null : request.comment().strip();
        review.setComment(comment == null || comment.isBlank() ? null : comment);
        review.setCreatedAt(clock.instant());
        // UNIQUE(event_id, user_id) es la protección definitiva ante envíos simultáneos.
        return toResponse(reviewRepository.saveAndFlush(review));
    }

    private EventReviewEligibilityResponse eligibility(Event event, User user) {
        Long eventId = event.getId();
        if (!user.isActive()) return denied(eventId, "Tu cuenta no está activa.");
        if (reviewRepository.existsByEventIdAndUserId(eventId, user.getId())) {
            return denied(eventId, "Ya registraste una reseña para este evento.");
        }
        if (event.getSpecialist().getUser().getId().equals(user.getId())) {
            return denied(eventId, "El organizador no puede calificar su propio evento.");
        }
        boolean booked = bookingRepository.hasCompletedEventReservation(
                eventId, user.getId(), List.of(BookingStatus.CONFIRMADA, BookingStatus.COMPLETADA),
                EventOccurrenceStatus.CANCELADA, clock.instant());
        if (!booked) {
            return denied(eventId, "Necesitas una reserva propia confirmada o completada y que haya terminado la fecha reservada.");
        }
        return new EventReviewEligibilityResponse(eventId, true, null);
    }

    private EventReviewEligibilityResponse denied(Long eventId, String reason) {
        return new EventReviewEligibilityResponse(eventId, false, reason);
    }

    private Event publicEvent(Long id) {
        return eventRepository.findById(id)
                .filter(event -> event.getStatus() == EventStatus.PUBLICADO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado."));
    }

    private User authenticatedUser(Long userId) {
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión.");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cuenta no encontrada."));
        if (!user.isActive()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta no está activa.");
        return user;
    }

    private EventReviewResponse toResponse(EventReview review) {
        String name = publicName(review.getUser().getUsername());
        return new EventReviewResponse(review.getId(), review.getEvent().getId(), name,
                review.getStars(), review.getComment(), review.getCreatedAt());
    }
    private String publicName(String name) {
        return name == null || name.isBlank() || name.contains("@") ? "Usuario de OONA" : name;
    }
}

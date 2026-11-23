package com.hean.consigueventas.oonabe.event.itinerary.service;

import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.itinerary.dto.EventItineraryItemRequest;
import com.hean.consigueventas.oonabe.event.itinerary.dto.EventItineraryItemResponse;
import com.hean.consigueventas.oonabe.event.itinerary.entity.EventItineraryItem;
import com.hean.consigueventas.oonabe.event.itinerary.repository.EventItineraryItemRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventItineraryService {
    private final EventRepository eventRepository;
    private final EventItineraryItemRepository itineraryRepository;

    public EventItineraryService(
            EventRepository eventRepository,
            EventItineraryItemRepository itineraryRepository) {
        this.eventRepository = eventRepository;
        this.itineraryRepository = itineraryRepository;
    }

    @Transactional(readOnly = true)
    public List<EventItineraryItemResponse> getItinerary(Long eventId) {
        requireEvent(eventId);
        return itineraryRepository
                .findByEventIdOrderBySortOrderAscStartTimeAscIdAsc(eventId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<EventItineraryItemResponse> replaceItinerary(
            Long eventId,
            List<EventItineraryItemRequest> requests,
            Long userId,
            boolean admin) {

        Event event = requireEvent(eventId);

        if (!admin && !event.getSpecialist().getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "No puedes gestionar el itinerario de otro profesional.");
        }

        for (EventItineraryItemRequest item : requests) {
            if (!item.endTime().isAfter(item.startTime())) {
                throw new IllegalArgumentException(
                        "La hora de fin debe ser posterior a la hora de inicio: "
                                + item.title());
            }
        }

        List<EventItineraryItem> existing =
                itineraryRepository
                        .findByEventIdOrderBySortOrderAscStartTimeAscIdAsc(eventId);
        itineraryRepository.deleteAll(existing);

        List<EventItineraryItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            EventItineraryItemRequest request = requests.get(i);
            EventItineraryItem item = new EventItineraryItem();
            item.setEvent(event);
            item.setStartTime(request.startTime());
            item.setEndTime(request.endTime());
            item.setTitle(request.title().trim());
            item.setDescription(request.description());
            item.setSortOrder(i);
            items.add(item);
        }

        return itineraryRepository.saveAll(items)
                .stream().map(this::toResponse).toList();
    }

    private Event requireEvent(Long eventId) {
        return eventRepository.findDetailById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evento no encontrado con ID: " + eventId));
    }

    private EventItineraryItemResponse toResponse(EventItineraryItem item) {
        return new EventItineraryItemResponse(
                item.getId(),
                item.getStartTime(),
                item.getEndTime(),
                item.getTitle(),
                item.getDescription(),
                item.getSortOrder());
    }
}

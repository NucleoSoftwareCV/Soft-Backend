package com.hean.consigueventas.oonabe.event.service;

import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventImage;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.mapper.EventMapper;
import com.hean.consigueventas.oonabe.event.repository.EventImageRepository;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EventCardAssembler {

    private final EventMapper eventMapper;
    private final EventImageRepository eventImageRepository;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final Clock clock;

    public EventCardAssembler(
            EventMapper eventMapper,
            EventImageRepository eventImageRepository,
            EventOccurrenceRepository eventOccurrenceRepository,
            Clock clock) {
        this.eventMapper = eventMapper;
        this.eventImageRepository = eventImageRepository;
        this.eventOccurrenceRepository = eventOccurrenceRepository;
        this.clock = clock;
    }

    public Page<EventCardResponse> toPage(Page<Event> events) {
        return new PageImpl<>(
                toCards(events.getContent(), null),
                events.getPageable(),
                events.getTotalElements()
        );
    }

    public List<EventCardResponse> toCards(List<Event> events) {
        return toCards(events, clock.instant());
    }

    private List<EventCardResponse> toCards(List<Event> events, Instant notBefore) {
        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, String> coverUrls = findCoverUrls(events);
        Map<Long, List<EventOccurrence>> occurrences = findOccurrences(events);
        return events.stream()
                .map(event -> eventMapper.toCardResponse(
                        event,
                        coverUrls.get(event.getId()),
                        occurrences.getOrDefault(event.getId(), List.of()),
                        notBefore))
                .toList();
    }

    private Map<Long, String> findCoverUrls(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .distinct()
                .toList();

        Map<Long, String> coverUrls = new LinkedHashMap<>();
        for (EventImage image : eventImageRepository.findOrderedCandidatesByEventIds(eventIds)) {
            coverUrls.putIfAbsent(image.getEvent().getId(), image.getUrl());
        }
        return coverUrls;
    }

    private Map<Long, List<EventOccurrence>> findOccurrences(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .distinct()
                .toList();
        return eventOccurrenceRepository.findProgrammedByEventIds(eventIds).stream()
                .collect(Collectors.groupingBy(
                        occurrence -> occurrence.getEvent().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
    }
}

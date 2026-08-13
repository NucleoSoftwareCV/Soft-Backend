package com.hean.consigueventas.oonabe.event.service;

import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceCalendarResponse;
import com.hean.consigueventas.oonabe.event.entity.EventImage;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.mapper.EventOccurrenceMapper;
import com.hean.consigueventas.oonabe.event.repository.EventImageRepository;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventOccurrenceService {

    private final EventOccurrenceRepository occurrenceRepository;
    private final EventImageRepository eventImageRepository;
    private final EventOccurrenceMapper mapper;

    public EventOccurrenceService(
            EventOccurrenceRepository occurrenceRepository,
            EventImageRepository eventImageRepository,
            EventOccurrenceMapper mapper
    ) {
        this.occurrenceRepository = occurrenceRepository;
        this.eventImageRepository = eventImageRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<EventOccurrenceAdminResponse> getAllOccurrences(Pageable pageable) {
        return occurrenceRepository.findAll(pageable)
                .map(mapper::toAdminDto);
    }

    @Transactional(readOnly = true)
    public List<EventOccurrenceCalendarResponse> getPublicCalendar(
            Long specialistId,
            Instant from,
            Instant to
    ) {
        List<EventOccurrence> occurrences =
                occurrenceRepository.findPublicCalendarOccurrences(specialistId, from, to);

        if (occurrences.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = occurrences.stream()
                .map(occurrence -> occurrence.getEvent().getId())
                .distinct()
                .toList();

        Map<Long, String> coverUrls = new LinkedHashMap<>();
        for (EventImage image : eventImageRepository.findOrderedCandidatesByEventIds(eventIds)) {
            coverUrls.putIfAbsent(image.getEvent().getId(), image.getUrl());
        }

        return occurrences.stream()
                .map(occurrence -> new EventOccurrenceCalendarResponse(
                        occurrence.getId(),
                        occurrence.getEvent().getId(),
                        occurrence.getEvent().getTitle(),
                        coverUrls.get(occurrence.getEvent().getId()),
                        occurrence.getStartsAt()
                ))
                .toList();
    }
}

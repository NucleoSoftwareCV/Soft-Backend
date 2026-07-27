package com.hean.consigueventas.oonabe.event.service;

import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.mapper.EventMapper;
import com.hean.consigueventas.oonabe.event.repository.EventImageRepository;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventCardAssemblerTest {

    @Test
    void loadsCoverCandidatesOnceForTheWholeCardBatch() {
        EventMapper eventMapper = mock(EventMapper.class);
        EventImageRepository imageRepository = mock(EventImageRepository.class);
        EventOccurrenceRepository occurrenceRepository = mock(EventOccurrenceRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-27T08:00:00Z"), ZoneOffset.UTC);
        EventCardAssembler assembler = new EventCardAssembler(
                eventMapper,
                imageRepository,
                occurrenceRepository,
                clock);
        Event first = event(1L);
        Event second = event(2L);
        when(imageRepository.findOrderedCandidatesByEventIds(anyCollection())).thenReturn(List.of());
        when(occurrenceRepository.findProgrammedByEventIds(anyCollection())).thenReturn(List.of());

        assembler.toCards(List.of(first, second));

        verify(imageRepository).findOrderedCandidatesByEventIds(anyCollection());
        verify(occurrenceRepository).findProgrammedByEventIds(anyCollection());
    }

    private Event event(Long id) {
        Event event = new Event();
        event.setId(id);
        return event;
    }
}

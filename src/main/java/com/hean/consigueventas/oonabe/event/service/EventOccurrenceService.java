package com.hean.consigueventas.oonabe.event.service;

import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.mapper.EventOccurrenceMapper;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventOccurrenceService {

    private final EventOccurrenceRepository occurrenceRepository;
    private final EventOccurrenceMapper mapper;

    public EventOccurrenceService(
            EventOccurrenceRepository occurrenceRepository,
            EventOccurrenceMapper mapper
    ) {
        this.occurrenceRepository = occurrenceRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<EventOccurrenceAdminResponse> getAllOccurrences(Pageable pageable) {
        return occurrenceRepository.findAll(pageable)
                .map(mapper::toAdminDto);
    }
}

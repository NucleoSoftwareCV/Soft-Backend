package com.hean.consigueventas.oonabe.event.service;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.dto.request.CreateEventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.CreateEventResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventDetailResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventResponse;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.entity.MeetingLink;
import com.hean.consigueventas.oonabe.event.mapper.EventMapper;
import com.hean.consigueventas.oonabe.event.mapper.EventOccurrenceMapper;
import com.hean.consigueventas.oonabe.event.mapper.MeetingLinkMapper;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.repository.MeetingLinkRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.mapper.LocationMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventOccurrenceRepository occurrenceRepository;
    private final MeetingLinkRepository meetingLinkRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final SpecialistProfileRepository specialistProfileRepository;

    private final EventMapper eventMapper;
    private final EventOccurrenceMapper occurrenceMapper;
    private final MeetingLinkMapper meetingLinkMapper;
    private final LocationMapper locationMapper;

    public EventService(EventRepository eventRepository,
                        EventOccurrenceRepository occurrenceRepository,
                        MeetingLinkRepository meetingLinkRepository,
                        LocationRepository locationRepository,
                        CategoryRepository categoryRepository,
                        SpecialistProfileRepository specialistProfileRepository,
                        EventMapper eventMapper,
                        EventOccurrenceMapper occurrenceMapper,
                        MeetingLinkMapper meetingLinkMapper,
                        LocationMapper locationMapper) {
        this.eventRepository = eventRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.meetingLinkRepository = meetingLinkRepository;
        this.locationRepository = locationRepository;
        this.categoryRepository = categoryRepository;
        this.specialistProfileRepository = specialistProfileRepository;
        this.eventMapper = eventMapper;
        this.occurrenceMapper = occurrenceMapper;
        this.meetingLinkMapper = meetingLinkMapper;
        this.locationMapper = locationMapper;
    }


    @Transactional
    public CreateEventResponse create(CreateEventUpsertRequest request) {
        // 1. Crear evento
        Event event = eventMapper.toEntity(request.event());

        // 2. Validar y asignar categoría
        Category category = categoryRepository.findById(request.event().categoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        event.setCategory(category);

        // 2.5 Validar y asignar especialista/organizador
        SpecialistProfile specialist = specialistProfileRepository.findById(request.event().specialistId())
                .orElseThrow(() -> new RuntimeException("Especialista no encontrado"));
        event.setSpecialist(specialist);

        // 3. Guardar evento
        Event savedEvent = eventRepository.save(event);

        // 4. Crear ocurrencia
        EventOccurrence occurrence = occurrenceMapper.toEntity(request.occurrence());
        occurrence.setEvent(savedEvent);

        // 5. Procesar según modalidad
        if (request.event().modality() == EventModality.ONLINE) {
            processOnlineEvent(occurrence, request);
        } else if (request.event().modality() == EventModality.PRESENCIAL) {
            processInPersonEvent(occurrence, request);
        }

        // 6. Guardar ocurrencia
        EventOccurrence savedOccurrence = occurrenceRepository.save(occurrence);

        // 7. Construir respuesta
        EventResponse eventResponse = eventMapper.toResponse(savedEvent);
        EventOccurrenceResponse occurrenceResponse = occurrenceMapper.toResponse(savedOccurrence);

        return new CreateEventResponse(
                eventResponse,
                occurrenceResponse,
                "Evento creado exitosamente"
        );
    }

    @Transactional(readOnly = true)
    public List<EventDetailResponse> getAllActiveEvents() {
        return eventRepository.findAll()
                .stream()
                .map(eventMapper::toDetailResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        return eventMapper.toDetailResponse(event);
    }

    private void processOnlineEvent(EventOccurrence occurrence, CreateEventUpsertRequest request) {
        if (request.occurrence().meetingLink() == null) {
            throw new IllegalArgumentException("Los datos de la reunión son obligatorios para eventos online");
        }

        MeetingLink link = meetingLinkMapper.toEntity(request.occurrence().meetingLink());
        link.setEventOccurrence(occurrence);

        meetingLinkRepository.save(link);
        occurrence.setMeetingLink(link);
    }

    private void processInPersonEvent(EventOccurrence occurrence, CreateEventUpsertRequest request) {
        if (request.occurrence().location() == null) {
            throw new IllegalArgumentException("Los datos de la ubicación son obligatorios para eventos presenciales");
        }

        Location location = locationMapper.toEntity(request.occurrence().location());
        locationRepository.save(location);
        occurrence.setLocation(location);
    }
}

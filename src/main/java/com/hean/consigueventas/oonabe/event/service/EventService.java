package com.hean.consigueventas.oonabe.event.service;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.dto.request.CreateEventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.dto.response.CreateEventResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
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
import com.hean.consigueventas.oonabe.event.specification.EventSpecification;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.mapper.LocationMapper;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

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
        Event event = eventMapper.toEntity(request.event());
        event.setCurrency(request.event().currency().toUpperCase(Locale.ROOT));

        Category category = categoryRepository.findById(request.event().categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " + request.event().categoryId()));
        event.setCategory(category);

        SpecialistProfile specialist = specialistProfileRepository.findById(request.event().specialistId())
                .orElseThrow(() -> new ResourceNotFoundException("Especialista no encontrado con ID: " + request.event().specialistId()));
        event.setSpecialist(specialist);

        Event savedEvent = eventRepository.save(event);
        EventOccurrence occurrence = occurrenceMapper.toEntity(request.occurrence());
        occurrence.setEvent(savedEvent);

        if (request.event().modality() == EventModality.ONLINE) {
            processOnlineEvent(occurrence, request);
        } else if (request.event().modality() == EventModality.PRESENCIAL) {
            processInPersonEvent(occurrence, request);
        }

        EventOccurrence savedOccurrence = occurrenceRepository.save(occurrence);
        EventResponse eventResponse = eventMapper.toResponse(savedEvent);
        EventOccurrenceResponse occurrenceResponse = occurrenceMapper.toResponse(savedOccurrence);

        return new CreateEventResponse(
                eventResponse,
                occurrenceResponse,
                "Evento creado exitosamente"
        );
    }

    @Transactional(readOnly = true)
    public Page<EventCardResponse> getAllActiveEvents(EventFilterRequest filter, Pageable pageable) {
        Sort.Order startsAtOrder = startsAtOrder(pageable);
        Specification<Event> spec = EventSpecification.publicListing(
                filter,
                startsAtOrder == null ? null : startsAtOrder.getDirection()
        );
        Pageable repositoryPageable = withoutStartsAtSort(pageable);

        return eventRepository.findAll(spec, repositoryPageable)
                .map(eventMapper::toCardResponse);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long id) {
        Event event = getEventOrThrow(id);
        return eventMapper.toDetailResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventCardResponse> getSimilarEvents(Long id, Pageable pageable) {
        Event event = getEventOrThrow(id);
        Specification<Event> spec = EventSpecification.isPublished()
                .and(EventSpecification.excludeEvent(event.getId()))
                .and(EventSpecification.hasCategory(event.getCategory().getId()))
                .and(EventSpecification.hasDifferentSpecialist(event.getSpecialist().getId()))
                .and(orderByStartsAtIfRequested(pageable));

        return eventRepository.findAll(spec, withoutStartsAtSort(pageable))
                .map(eventMapper::toCardResponse);
    }

    @Transactional(readOnly = true)
    public Page<EventCardResponse> getOrganizerEvents(Long id, Pageable pageable) {
        Event event = getEventOrThrow(id);
        Specification<Event> spec = EventSpecification.isPublished()
                .and(EventSpecification.excludeEvent(event.getId()))
                .and(EventSpecification.hasSpecialist(event.getSpecialist().getId()))
                .and(orderByStartsAtIfRequested(pageable));

        return eventRepository.findAll(spec, withoutStartsAtSort(pageable))
                .map(eventMapper::toCardResponse);
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));
    }

    private Specification<Event> orderByStartsAtIfRequested(Pageable pageable) {
        Sort.Order startsAtOrder = startsAtOrder(pageable);
        return startsAtOrder == null
                ? (root, query, cb) -> cb.conjunction()
                : EventSpecification.orderByNextProgrammedOccurrence(startsAtOrder.getDirection());
    }

    private Sort.Order startsAtOrder(Pageable pageable) {
        return pageable.getSort().getOrderFor("startsAt");
    }

    private Pageable withoutStartsAtSort(Pageable pageable) {
        if (startsAtOrder(pageable) == null) {
            return pageable;
        }

        List<Sort.Order> orders = pageable.getSort().stream()
                .filter(order -> !"startsAt".equals(order.getProperty()))
                .toList();
        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private void processOnlineEvent(EventOccurrence occurrence, CreateEventUpsertRequest request) {
        if (request.occurrence().meetingLink() == null) {
            throw new BusinessLogicException("Los datos de la reunion son obligatorios para eventos online.");
        }

        MeetingLink link = meetingLinkMapper.toEntity(request.occurrence().meetingLink());
        link.setEventOccurrence(occurrence);

        meetingLinkRepository.save(link);
        occurrence.setMeetingLink(link);
    }

    private void processInPersonEvent(EventOccurrence occurrence, CreateEventUpsertRequest request) {
        if (request.occurrence().location() == null) {
            throw new BusinessLogicException("Los datos de la ubicacion son obligatorios para eventos presenciales.");
        }

        Location location = locationMapper.toEntity(request.occurrence().location());
        locationRepository.save(location);
        occurrence.setLocation(location);
    }
}

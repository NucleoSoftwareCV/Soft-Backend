package com.hean.consigueventas.oonabe.event.service;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.dto.request.CreateEventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceStatusUpdateRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventStatusUpdateRequest;
import com.hean.consigueventas.oonabe.event.dto.request.EventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.CreateEventResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventDetailResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventManagementResponse;
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
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
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
import org.springframework.security.access.AccessDeniedException;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventOccurrenceRepository occurrenceRepository;
    private final MeetingLinkRepository meetingLinkRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final ExperienceTypeRepository experienceTypeRepository;
    private final SpecialistProfileRepository specialistProfileRepository;

    private final EventMapper eventMapper;
    private final EventOccurrenceMapper occurrenceMapper;
    private final MeetingLinkMapper meetingLinkMapper;
    private final LocationMapper locationMapper;
    private final EventCardAssembler eventCardAssembler;

    public EventService(EventRepository eventRepository,
                        EventOccurrenceRepository occurrenceRepository,
                        MeetingLinkRepository meetingLinkRepository,
                        LocationRepository locationRepository,
                        CategoryRepository categoryRepository,
                        ExperienceTypeRepository experienceTypeRepository,
                        SpecialistProfileRepository specialistProfileRepository,
                        EventMapper eventMapper,
                        EventOccurrenceMapper occurrenceMapper,
                        MeetingLinkMapper meetingLinkMapper,
                        LocationMapper locationMapper,
                        EventCardAssembler eventCardAssembler) {
        this.eventRepository = eventRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.meetingLinkRepository = meetingLinkRepository;
        this.locationRepository = locationRepository;
        this.categoryRepository = categoryRepository;
        this.experienceTypeRepository = experienceTypeRepository;
        this.specialistProfileRepository = specialistProfileRepository;
        this.eventMapper = eventMapper;
        this.occurrenceMapper = occurrenceMapper;
        this.meetingLinkMapper = meetingLinkMapper;
        this.locationMapper = locationMapper;
        this.eventCardAssembler = eventCardAssembler;
    }

    @Transactional
    public CreateEventResponse create(CreateEventUpsertRequest request, Long userId, boolean admin) {
        Event event = eventMapper.toEntity(request.event());
        event.setCurrency(request.event().currency().toUpperCase(Locale.ROOT));

        event.setCategory(resolveActiveCategory(request.event().categoryId()));
        event.setExperienceType(resolveActiveExperienceType(request.event().experienceTypeId()));

        SpecialistProfile specialist = resolveManagedSpecialist(request.event().specialistId(), userId, admin);
        event.setSpecialist(specialist);

        Event savedEvent = eventRepository.save(event);
        EventOccurrence occurrence = occurrenceMapper.toEntity(request.occurrence());
        occurrence.setEvent(savedEvent);
        configureOccurrence(occurrence, savedEvent, request.occurrence());
        EventOccurrence savedOccurrence = persistOccurrence(occurrence);
        EventResponse eventResponse = eventMapper.toResponse(savedEvent);
        EventOccurrenceResponse occurrenceResponse = occurrenceMapper.toResponse(savedOccurrence);

        return new CreateEventResponse(
                eventResponse,
                occurrenceResponse,
                "Evento creado exitosamente"
        );
    }

    @Transactional(readOnly = true)
    public Page<EventManagementResponse> getMyEvents(Long userId, Pageable pageable) {
        return eventRepository.findBySpecialistUserId(userId, pageable)
                .map(this::toManagementResponse);
    }

    @Transactional(readOnly = true)
    public EventManagementResponse getManagementEvent(Long id, Long userId, boolean admin) {
        return toManagementResponse(getManagedEvent(id, userId, admin));
    }

    @Transactional
    public EventManagementResponse updateEvent(
            Long id,
            EventUpsertRequest request,
            Long userId,
            boolean admin) {
        Event event = getManagedEvent(id, userId, admin);
        eventMapper.updateEntity(request, event);
        event.setCurrency(request.currency().toUpperCase(Locale.ROOT));
        event.setCategory(resolveActiveCategory(request.categoryId()));
        event.setExperienceType(resolveActiveExperienceType(request.experienceTypeId()));
        if (admin && !event.getSpecialist().getId().equals(request.specialistId())) {
            event.setSpecialist(specialistProfileRepository.findById(request.specialistId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Especialista no encontrado con ID: " + request.specialistId())));
        }
        return toManagementResponse(eventRepository.save(event));
    }

    @Transactional
    public EventManagementResponse updateEventStatus(
            Long id,
            EventStatusUpdateRequest request,
            Long userId,
            boolean admin) {
        Event event = getManagedEvent(id, userId, admin);
        event.setStatus(request.status());
        return toManagementResponse(eventRepository.save(event));
    }

    @Transactional
    public EventOccurrenceResponse addOccurrence(
            Long eventId,
            EventOccurrenceRequest request,
            Long userId,
            boolean admin) {
        Event event = getManagedEvent(eventId, userId, admin);
        EventOccurrence occurrence = occurrenceMapper.toEntity(request);
        occurrence.setEvent(event);
        configureOccurrence(occurrence, event, request);
        return occurrenceMapper.toResponse(persistOccurrence(occurrence));
    }

    @Transactional
    public EventOccurrenceResponse updateOccurrence(
            Long occurrenceId,
            EventOccurrenceRequest request,
            Long userId,
            boolean admin) {
        EventOccurrence occurrence = getManagedOccurrence(occurrenceId, userId, admin);
        occurrence.setStartsAt(request.startsAt());
        occurrence.setEndsAt(request.endsAt());
        occurrence.setCapacity(request.capacity());
        configureOccurrence(occurrence, occurrence.getEvent(), request);
        return occurrenceMapper.toResponse(persistOccurrence(occurrence));
    }

    @Transactional
    public EventOccurrenceResponse updateOccurrenceStatus(
            Long occurrenceId,
            EventOccurrenceStatusUpdateRequest request,
            Long userId,
            boolean admin) {
        EventOccurrence occurrence = getManagedOccurrence(occurrenceId, userId, admin);
        occurrence.setStatus(request.status());
        return occurrenceMapper.toResponse(occurrenceRepository.save(occurrence));
    }

    @Transactional(readOnly = true)
    public Page<EventCardResponse> getAllActiveEvents(EventFilterRequest filter, Pageable pageable) {
        Sort.Order startsAtOrder = startsAtOrder(pageable);
        Specification<Event> spec = EventSpecification.publicListing(
                filter,
                startsAtOrder == null ? null : startsAtOrder.getDirection()
        );
        Pageable repositoryPageable = withoutStartsAtSort(pageable);

        return eventCardAssembler.toPage(eventRepository.findAll(spec, repositoryPageable));
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

        return eventCardAssembler.toPage(eventRepository.findAll(spec, withoutStartsAtSort(pageable)));
    }

    @Transactional(readOnly = true)
    public Page<EventCardResponse> getOrganizerEvents(Long id, Pageable pageable) {
        Event event = getEventOrThrow(id);
        Specification<Event> spec = EventSpecification.isPublished()
                .and(EventSpecification.excludeEvent(event.getId()))
                .and(EventSpecification.hasSpecialist(event.getSpecialist().getId()))
                .and(orderByStartsAtIfRequested(pageable));

        return eventCardAssembler.toPage(eventRepository.findAll(spec, withoutStartsAtSort(pageable)));
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));
    }

    private Event getManagedEvent(Long id, Long userId, boolean admin) {
        Event event = eventRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));
        if (!admin && !event.getSpecialist().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No puedes gestionar eventos de otro profesional.");
        }
        return event;
    }

    private EventOccurrence getManagedOccurrence(Long id, Long userId, boolean admin) {
        EventOccurrence occurrence = occurrenceRepository.findManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocurrencia no encontrada con ID: " + id));
        if (!admin && !occurrence.getEvent().getSpecialist().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("No puedes gestionar ocurrencias de otro profesional.");
        }
        return occurrence;
    }

    private SpecialistProfile resolveManagedSpecialist(Long requestedId, Long userId, boolean admin) {
        if (admin) {
            return specialistProfileRepository.findById(requestedId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Especialista no encontrado con ID: " + requestedId));
        }
        SpecialistProfile specialist = specialistProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario autenticado no tiene perfil profesional."));
        if (!specialist.getId().equals(requestedId)) {
            throw new AccessDeniedException("No puedes crear eventos para otro profesional.");
        }
        return specialist;
    }

    private ExperienceType resolveActiveExperienceType(Long id) {
        ExperienceType type = experienceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de experiencia no encontrado con ID: " + id));
        if (!type.isActive()) {
            throw new BusinessLogicException("El tipo de experiencia seleccionado esta inactivo.");
        }
        return type;
    }

    private Category resolveActiveCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria no encontrada con ID: " + id));
        if (!category.isActive()) {
            throw new BusinessLogicException("La categoria seleccionada esta inactiva.");
        }
        return category;
    }

    private EventManagementResponse toManagementResponse(Event event) {
        List<EventOccurrenceResponse> occurrences = occurrenceRepository
                .findByEventIdOrderByStartsAtAsc(event.getId())
                .stream()
                .map(occurrenceMapper::toResponse)
                .toList();
        return new EventManagementResponse(
                eventMapper.toDetailResponse(event),
                event.getStatus(),
                event.getSpecialist().getId(),
                occurrences);
    }

    private void configureOccurrence(
            EventOccurrence occurrence,
            Event event,
            EventOccurrenceRequest request) {
        occurrence.setLocation(null);
        occurrence.setMeetingLink(null);
        if (event.getModality() == EventModality.ONLINE) {
            if (request.meetingLink() == null) {
                throw new BusinessLogicException(
                        "Los datos de la reunion son obligatorios para eventos online.");
            }
            MeetingLink link = meetingLinkMapper.toEntity(request.meetingLink());
            link.setEventOccurrence(occurrence);
            occurrence.setMeetingLink(link);
        } else {
            if (request.location() == null) {
                throw new BusinessLogicException(
                        "Los datos de la ubicacion son obligatorios para eventos presenciales.");
            }
            Location location = locationMapper.toEntity(request.location());
            occurrence.setLocation(locationRepository.save(location));
        }
    }

    private EventOccurrence persistOccurrence(EventOccurrence occurrence) {
        MeetingLink link = occurrence.getMeetingLink();
        if (link == null) {
            return occurrenceRepository.save(occurrence);
        }

        occurrence.setMeetingLink(null);
        EventOccurrence saved = occurrenceRepository.save(occurrence);
        link.setEventOccurrence(saved);
        MeetingLink savedLink = meetingLinkRepository.save(link);
        saved.setMeetingLink(savedLink);
        return occurrenceRepository.save(saved);
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

}

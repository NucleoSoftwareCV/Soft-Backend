package com.hean.consigueventas.oonabe.event.service.impl;

import com.hean.consigueventas.oonabe.event.service.IEventOccurrenceService;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrencePublicResponse;
import com.hean.consigueventas.oonabe.event.dto.request.EventOccurrenceUpsertRequest;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.mapper.EventOccurrenceMapper;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class EventOccurrenceServiceImpl implements IEventOccurrenceService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Lima");

    private final EventOccurrenceRepository occurrenceRepository;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final EventOccurrenceMapper mapper;

    public EventOccurrenceServiceImpl(
            EventOccurrenceRepository occurrenceRepository,
            EventRepository eventRepository,
            LocationRepository locationRepository,
            EventOccurrenceMapper mapper
    ) {
        this.occurrenceRepository = occurrenceRepository;
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventOccurrenceAdminResponse> getAllOccurrences() {
        return occurrenceRepository.findAll()
                .stream()
                .map(mapper::toAdminDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventOccurrencePublicResponse> getPublicOccurrences() {
        return occurrenceRepository.findAll()
                .stream()
                .map(mapper::toPublicDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventOccurrencePublicResponse> getPublicOccurrencesByDate(LocalDate date) {
        return getPublicOccurrencesByDateRange(date, date);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventOccurrencePublicResponse> getPublicOccurrencesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessLogicException("La fecha final no puede ser anterior a la fecha inicial");
        }

        return occurrenceRepository
                .findByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                        at(startDate, LocalTime.MIN),
                        at(endDate.plusDays(1), LocalTime.MIN)
                )
                .stream()
                .map(mapper::toPublicDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventOccurrenceAdminResponse> getOccurrencesByEvent(Long eventId) {
        return occurrenceRepository.findByEventIdOrderByStartsAtAsc(eventId)
                .stream()
                .map(mapper::toAdminDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public EventOccurrenceAdminResponse getOccurrenceById(Long id) {
        return occurrenceRepository.findById(id)
                .map(mapper::toAdminDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ocurrencia de evento no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<EventOccurrenceAdminResponse> filterOccurrences(
            String dateFilter,
            String timeFilter,
            LocalDate selectedDate
    ) {
        DateRange dateRange = resolveDateRange(dateFilter, selectedDate);
        TimeRange timeRange = resolveTimeRange(timeFilter);
        Instant startsAtFrom = at(dateRange.startDate(), LocalTime.MIN);
        Instant startsAtTo = at(dateRange.endDate(), LocalTime.MIN);

        return occurrenceRepository
                .findByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(startsAtFrom, startsAtTo)
                .stream()
                .filter(occurrence -> matchesTimeRange(occurrence.getStartsAt(), timeRange))
                .map(mapper::toAdminDto)
                .toList();
    }

    @Transactional
    @Override
    public EventOccurrenceAdminResponse createOccurrence(EventOccurrenceUpsertRequest dto) {
        EventOccurrence occurrence = new EventOccurrence();
        applyUpsertDto(dto, occurrence);
        return mapper.toAdminDto(occurrenceRepository.save(occurrence));
    }

    @Transactional
    @Override
    public EventOccurrenceAdminResponse updateOccurrence(Long id, EventOccurrenceUpsertRequest dto) {
        EventOccurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocurrencia de evento no encontrada con ID: " + id));
        applyUpsertDto(dto, occurrence);
        return mapper.toAdminDto(occurrenceRepository.save(occurrence));
    }

    @Transactional
    @Override
    public void deleteOccurrence(Long id) {
        EventOccurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocurrencia de evento no encontrada con ID: " + id));
        occurrenceRepository.delete(occurrence);
    }

    private void applyUpsertDto(EventOccurrenceUpsertRequest dto, EventOccurrence occurrence) {
        if (!dto.endsAt().isAfter(dto.startsAt())) {
            throw new BusinessLogicException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        int reservedSpots = dto.reservedSpots() == null ? 0 : dto.reservedSpots();
        if (reservedSpots > dto.capacity()) {
            throw new BusinessLogicException("Los cupos reservados no pueden superar la capacidad");
        }

        occurrence.setEvent(eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + dto.eventId())));
        occurrence.setStartsAt(dto.startsAt());
        occurrence.setEndsAt(dto.endsAt());
        occurrence.setLocation(resolveLocation(dto.locationId()));
        occurrence.setCapacity(dto.capacity());
        occurrence.setReservedSpots(reservedSpots);
        occurrence.setStatus(dto.status() == null ? EventOccurrenceStatus.PROGRAMADA : dto.status());
        occurrence.setVirtualUrl(dto.virtualUrl());
    }

    private Location resolveLocation(Long locationId) {
        if (locationId == null) {
            return null;
        }
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada con ID: " + locationId));
    }

    private DateRange resolveDateRange(String dateFilter, LocalDate selectedDate) {
        String normalizedDateFilter = normalizeFilter(dateFilter == null || dateFilter.isBlank() ? "HOY" : dateFilter);
        LocalDate today = LocalDate.now(ZONE_ID);

        return switch (normalizedDateFilter) {
            case "HOY" -> new DateRange(today, today.plusDays(1));
            case "MANANA" -> new DateRange(today.plusDays(1), today.plusDays(2));
            case "ESTE_FINDE" -> weekendRange(today);
            case "ESTA_SEMANA" -> new DateRange(
                    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            );
            case "PROXIMA_SEMANA" -> {
                LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                yield new DateRange(nextMonday, nextMonday.plusWeeks(1));
            }
            case "PROXIMO_MES" -> {
                LocalDate nextMonth = today.plusMonths(1).withDayOfMonth(1);
                yield new DateRange(nextMonth, nextMonth.plusMonths(1));
            }
            case "ELEGIR_FECHA" -> {
                if (selectedDate == null) {
                    throw new BusinessLogicException("Seleccione una fecha");
                }
                yield new DateRange(selectedDate, selectedDate.plusDays(1));
            }
            default -> throw new BusinessLogicException("Filtro de fecha no valido");
        };
    }

    private DateRange weekendRange(LocalDate today) {
        if (today.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return new DateRange(today, today.plusDays(2));
        }
        if (today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return new DateRange(today, today.plusDays(1));
        }
        LocalDate saturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        return new DateRange(saturday, saturday.plusDays(2));
    }

    private TimeRange resolveTimeRange(String timeFilter) {
        if (timeFilter == null || timeFilter.isBlank()) {
            return null;
        }

        return switch (normalizeFilter(timeFilter)) {
            case "MANANA" -> new TimeRange(LocalTime.of(6, 0), LocalTime.of(12, 0));
            case "MEDIODIA" -> new TimeRange(LocalTime.of(12, 0), LocalTime.of(16, 0));
            case "TARDE" -> new TimeRange(LocalTime.of(16, 0), LocalTime.of(20, 0));
            case "NOCHE" -> new TimeRange(LocalTime.of(20, 0), null);
            default -> throw new BusinessLogicException("Filtro de hora no valido");
        };
    }

    private boolean matchesTimeRange(Instant startsAt, TimeRange timeRange) {
        if (timeRange == null) {
            return true;
        }

        LocalTime localStartTime = startsAt.atZone(ZONE_ID).toLocalTime();
        boolean startsAfterOrAtFrom = !localStartTime.isBefore(timeRange.startTime());
        boolean startsBeforeTo = timeRange.endTime() == null || localStartTime.isBefore(timeRange.endTime());
        return startsAfterOrAtFrom && startsBeforeTo;
    }

    private Instant at(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(ZONE_ID).toInstant();
    }

    private String normalizeFilter(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace(" ", "_")
                .toUpperCase();
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }

    private record TimeRange(LocalTime startTime, LocalTime endTime) {
    }
}

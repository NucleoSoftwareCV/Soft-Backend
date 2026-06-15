package com.hean.consigueventas.oonabe.eventSchedule.service;

import com.hean.consigueventas.oonabe.eventSchedule.dto.EventScheduleDTO;
import com.hean.consigueventas.oonabe.eventSchedule.mapper.EventScheduleMapper;
import com.hean.consigueventas.oonabe.eventSchedule.repository.EventScheduleRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class EventScheduleService {

    private final EventScheduleRepository eventScheduleRepository;
    private final EventScheduleMapper eventScheduleMapper;

    private static final ZoneId ZONE_ID = ZoneId.of("America/Lima");

    public EventScheduleService(
            EventScheduleRepository eventScheduleRepository,
            EventScheduleMapper eventScheduleMapper
    ) {
        this.eventScheduleRepository = eventScheduleRepository;
        this.eventScheduleMapper = eventScheduleMapper;
    }

    public List<EventScheduleDTO> getAllSchedules() {
        return eventScheduleRepository.findAll()
                .stream()
                .map(eventScheduleMapper::toDto)
                .toList();
    }

    public EventScheduleDTO getScheduleById(Long id) {
        return eventScheduleRepository.findById(id)
                .map(eventScheduleMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Horario de evento no encontrado con ID: " + id));
    }

    public EventScheduleDTO createSchedule(EventScheduleDTO eventScheduleDTO) {
        var eventSchedule = eventScheduleMapper.toEntity(eventScheduleDTO);
        var savedEventSchedule = eventScheduleRepository.save(eventSchedule);

        return eventScheduleMapper.toDto(savedEventSchedule);
    }

    public EventScheduleDTO updateSchedule(Long id, EventScheduleDTO eventScheduleDTO) {
        var existingEventSchedule = eventScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario de evento no encontrado con ID: " + id));

        eventScheduleMapper.updateEntityFromDto(eventScheduleDTO, existingEventSchedule);

        var updatedEventSchedule = eventScheduleRepository.save(existingEventSchedule);

        return eventScheduleMapper.toDto(updatedEventSchedule);
    }

    public void deleteSchedule(Long id) {
        var existingEventSchedule = eventScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario de evento no encontrado con ID: " + id));

        eventScheduleRepository.delete(existingEventSchedule);
    }

    public List<EventScheduleDTO> filterSchedules(
            String dateFilter,
            String timeFilter,
            LocalDate selectedDate
    ) {
        if (dateFilter == null || dateFilter.isBlank()) {
            dateFilter = "HOY";
        }

        String normalizedDateFilter = normalizeFilter(dateFilter);

        LocalDate today = LocalDate.now(ZONE_ID);

        LocalDate startDate;
        LocalDate endDate;

        switch (normalizedDateFilter) {
            case "HOY" -> {
                startDate = today;
                endDate = today;
            }

            case "MANANA" -> {
                startDate = today.plusDays(1);
                endDate = today.plusDays(1);
            }

            case "ESTE_FINDE" -> {
                if (today.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    startDate = today;
                    endDate = today.plusDays(1);
                } else if (today.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    startDate = today;
                    endDate = today;
                } else {
                    startDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
                    endDate = startDate.plusDays(1);
                }
            }

            case "ESTA_SEMANA" -> {
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            }

            case "PROXIMA_SEMANA" -> {
                startDate = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                endDate = startDate.plusDays(6);
            }

            case "PROXIMO_MES" -> {
                LocalDate nextMonth = today.plusMonths(1);
                startDate = nextMonth.withDayOfMonth(1);
                endDate = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth());
            }

            case "ELEGIR_FECHA" -> {
                if (selectedDate == null) {
                    throw new IllegalArgumentException("Seleccione una fecha");
                }

                startDate = selectedDate;
                endDate = selectedDate;
            }

            default -> throw new IllegalArgumentException("Fecha no válido");
        }

        if (timeFilter == null || timeFilter.isBlank()) {
            return eventScheduleRepository
                    .findByDateBetweenOrderByDateAscStartTimeAsc(startDate, endDate)
                    .stream()
                    .map(eventScheduleMapper::toDto)
                    .toList();
        }

        String normalizedTimeFilter = normalizeFilter(timeFilter);

        LocalTime startTime;
        LocalTime endTime;

        switch (normalizedTimeFilter) {
            case "MANANA" -> {
                startTime = LocalTime.of(6, 0);
                endTime = LocalTime.of(12, 0);
            }

            case "MEDIODIA" -> {
                startTime = LocalTime.of(12, 0);
                endTime = LocalTime.of(16, 0);
            }

            case "TARDE" -> {
                startTime = LocalTime.of(16, 0);
                endTime = LocalTime.of(20, 0);
            }

            case "NOCHE" -> {
                startTime = LocalTime.of(20, 0);
                endTime = LocalTime.MAX;
            }

            default -> throw new IllegalArgumentException("Filtro de hora no válido.");
        }

        return eventScheduleRepository
                .findByDateBetweenAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByDateAscStartTimeAsc(
                        startDate,
                        endDate,
                        startTime,
                        endTime
                )
                .stream()
                .map(eventScheduleMapper::toDto)
                .toList();
    }

    private String normalizeFilter(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace(" ", "_")
                .toUpperCase();
    }
}
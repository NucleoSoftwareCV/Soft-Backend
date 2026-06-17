package com.hean.consigueventas.oonabe.event;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.event.dto.EventOccurrenceAdminDTO;
import com.hean.consigueventas.oonabe.event.dto.EventOccurrencePublicDTO;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.mapper.EventOccurrenceMapper;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.service.EventOccurrenceService;
import com.hean.consigueventas.oonabe.location.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventOccurrenceServiceTest {

    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");

    private final EventOccurrenceRepository occurrenceRepository = mock(EventOccurrenceRepository.class);
    private final EventRepository eventRepository = mock(EventRepository.class);
    private final LocationRepository locationRepository = mock(LocationRepository.class);
    private final EventOccurrenceMapper mapper = Mappers.getMapper(EventOccurrenceMapper.class);
    private final EventOccurrenceService service = new EventOccurrenceService(
            occurrenceRepository,
            eventRepository,
            locationRepository,
            mapper
    );

    @Test
    void filtersOccurrencesByDateRangeAndTimeWindowUsingInstants() {
        LocalDate date = LocalDate.of(2026, 6, 20);
        EventOccurrence occurrence = occurrenceAt(date, LocalTime.of(9, 0), LocalTime.of(11, 0));
        EventOccurrence afternoonOccurrence = occurrenceAt(date, LocalTime.of(17, 0), LocalTime.of(19, 0));
        when(occurrenceRepository.findByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                at(date, LocalTime.MIN),
                at(date.plusDays(1), LocalTime.MIN)
        )).thenReturn(List.of(occurrence, afternoonOccurrence));

        List<EventOccurrenceAdminDTO> result = service.filterOccurrences(
                "ELEGIR_FECHA",
                "MANANA",
                date
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().startsAt()).isEqualTo(at(date, LocalTime.of(9, 0)));
        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(occurrenceRepository).findByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                startCaptor.capture(),
                endCaptor.capture()
        );
        assertThat(startCaptor.getValue()).isEqualTo(at(date, LocalTime.MIN));
        assertThat(endCaptor.getValue()).isEqualTo(at(date.plusDays(1), LocalTime.MIN));
    }

    @Test
    void mapsAvailabilityFromCapacityAndReservedSpots() {
        EventOccurrence occurrence = occurrenceAt(
                LocalDate.of(2026, 6, 20),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0)
        );
        occurrence.setCapacity(30);
        occurrence.setReservedSpots(12);

        EventOccurrenceAdminDTO dto = mapper.toAdminDto(occurrence);

        assertThat(dto.capacity()).isEqualTo(30);
        assertThat(dto.reservedSpots()).isEqualTo(12);
        assertThat(dto.availableSpots()).isEqualTo(18);
        assertThat(dto.soldOut()).isFalse();
    }

    @Test
    void getsPublicOccurrencesByDateRangeUsingExclusiveEndDate() {
        LocalDate startDate = LocalDate.of(2026, 6, 20);
        LocalDate endDate = LocalDate.of(2026, 6, 22);
        EventOccurrence occurrence = occurrenceAt(startDate, LocalTime.of(18, 0), LocalTime.of(20, 0));
        when(occurrenceRepository.findByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                at(startDate, LocalTime.MIN),
                at(endDate.plusDays(1), LocalTime.MIN)
        )).thenReturn(List.of(occurrence));

        List<EventOccurrencePublicDTO> result = service.getPublicOccurrencesByDateRange(startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().eventTitle()).isEqualTo("Taller");
    }

    private static EventOccurrence occurrenceAt(LocalDate date, LocalTime startTime, LocalTime endTime) {
        Event event = new Event();
        event.setId(7L);
        event.setTitle("Taller");

        EventOccurrence occurrence = new EventOccurrence();
        occurrence.setId(11L);
        occurrence.setEvent(event);
        occurrence.setStartsAt(at(date, startTime));
        occurrence.setEndsAt(at(date, endTime));
        occurrence.setCapacity(20);
        occurrence.setReservedSpots(0);
        occurrence.setStatus(EventOccurrenceStatus.PROGRAMADA);
        return occurrence;
    }

    private static Instant at(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(LIMA_ZONE).toInstant();
    }
}

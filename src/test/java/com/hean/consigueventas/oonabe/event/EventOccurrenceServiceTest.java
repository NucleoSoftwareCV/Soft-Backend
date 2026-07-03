package com.hean.consigueventas.oonabe.event;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.event.dto.response.EventOccurrenceAdminResponse;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.mapper.EventOccurrenceMapper;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.service.EventOccurrenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    private final EventOccurrenceMapper mapper = Mappers.getMapper(EventOccurrenceMapper.class);
    private final EventOccurrenceService service = new EventOccurrenceService(
            occurrenceRepository,
            mapper
    );

    @Test
    void getsAllOccurrencesUsingPageable() {
        Pageable pageable = PageRequest.of(0, 20);
        EventOccurrence occurrence = occurrenceAt(
                LocalDate.of(2026, 6, 20),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0)
        );
        when(occurrenceRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(occurrence), pageable, 1));

        Page<EventOccurrenceAdminResponse> result = service.getAllOccurrences(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().eventTitle()).isEqualTo("Taller");
        verify(occurrenceRepository).findAll(pageable);
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

        EventOccurrenceAdminResponse dto = mapper.toAdminDto(occurrence);

        assertThat(dto.capacity()).isEqualTo(30);
        assertThat(dto.reservedSpots()).isEqualTo(12);
        assertThat(dto.availableSpots()).isEqualTo(18);
        assertThat(dto.soldOut()).isFalse();
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

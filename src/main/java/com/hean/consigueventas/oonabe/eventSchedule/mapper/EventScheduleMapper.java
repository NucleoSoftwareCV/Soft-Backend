package com.hean.consigueventas.oonabe.eventSchedule.mapper;

import com.hean.consigueventas.oonabe.eventSchedule.dto.EventScheduleDTO;
import com.hean.consigueventas.oonabe.eventSchedule.entity.EventSchedule;
import org.springframework.stereotype.Component;

@Component
public class EventScheduleMapper {

    public EventScheduleDTO toDto(EventSchedule eventSchedule) {
        return new EventScheduleDTO(
                eventSchedule.getId(),
                eventSchedule.getDate(),
                eventSchedule.getStartTime(),
                eventSchedule.getEndTime(),
                eventSchedule.isBooked(),
                eventSchedule.getMaxCapacity(),
                eventSchedule.getAvailableSpots()
        );
    }

    public EventSchedule toEntity(EventScheduleDTO eventScheduleDTO) {
        EventSchedule eventSchedule = new EventSchedule();

        eventSchedule.setDate(eventScheduleDTO.date());
        eventSchedule.setStartTime(eventScheduleDTO.startTime());
        eventSchedule.setEndTime(eventScheduleDTO.endTime());
        eventSchedule.setBooked(eventScheduleDTO.booked());
        eventSchedule.setMaxCapacity(eventScheduleDTO.maxCapacity());
        eventSchedule.setAvailableSpots(eventScheduleDTO.availableSpots());

        return eventSchedule;
    }

    public void updateEntityFromDto(EventScheduleDTO eventScheduleDTO, EventSchedule eventSchedule) {
        eventSchedule.setDate(eventScheduleDTO.date());
        eventSchedule.setStartTime(eventScheduleDTO.startTime());
        eventSchedule.setEndTime(eventScheduleDTO.endTime());
        eventSchedule.setBooked(eventScheduleDTO.booked());
        eventSchedule.setMaxCapacity(eventScheduleDTO.maxCapacity());
        eventSchedule.setAvailableSpots(eventScheduleDTO.availableSpots());
    }
}
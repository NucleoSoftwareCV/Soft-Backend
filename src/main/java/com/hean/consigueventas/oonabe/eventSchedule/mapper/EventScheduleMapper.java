package com.hean.consigueventas.oonabe.eventSchedule.mapper;

import com.hean.consigueventas.oonabe.eventSchedule.dto.EventScheduleAdminDTO;
import com.hean.consigueventas.oonabe.eventSchedule.dto.EventScheduleUsuarioDTO;
import com.hean.consigueventas.oonabe.eventSchedule.entity.EventSchedule;
import org.springframework.stereotype.Component;

@Component
public class EventScheduleMapper {

    public EventScheduleAdminDTO toAdminDto(EventSchedule eventSchedule) {
        return new EventScheduleAdminDTO(
                eventSchedule.getId(),
                eventSchedule.getDate(),
                eventSchedule.getStartTime(),
                eventSchedule.getEndTime(),
                eventSchedule.isBooked(),
                eventSchedule.getMaxCapacity(),
                eventSchedule.getAvailableSpots()
        );
    }

    public EventScheduleUsuarioDTO toUsuarioDto(EventSchedule eventSchedule) {
        return new EventScheduleUsuarioDTO(
                eventSchedule.getDate(),
                eventSchedule.getStartTime(),
                eventSchedule.getEndTime(),
                eventSchedule.isBooked(),
                eventSchedule.getMaxCapacity(),
                eventSchedule.getAvailableSpots()
        );
    }

    public EventSchedule toEntity(EventScheduleAdminDTO eventScheduleAdminDTO) {
        EventSchedule eventSchedule = new EventSchedule();

        eventSchedule.setDate(eventScheduleAdminDTO.date());
        eventSchedule.setStartTime(eventScheduleAdminDTO.startTime());
        eventSchedule.setEndTime(eventScheduleAdminDTO.endTime());
        eventSchedule.setBooked(eventScheduleAdminDTO.booked());
        eventSchedule.setMaxCapacity(eventScheduleAdminDTO.maxCapacity());
        eventSchedule.setAvailableSpots(eventScheduleAdminDTO.availableSpots());

        return eventSchedule;
    }

    public void updateEntityFromDto(EventScheduleAdminDTO eventScheduleAdminDTO, EventSchedule eventSchedule) {
        eventSchedule.setDate(eventScheduleAdminDTO.date());
        eventSchedule.setStartTime(eventScheduleAdminDTO.startTime());
        eventSchedule.setEndTime(eventScheduleAdminDTO.endTime());
        eventSchedule.setBooked(eventScheduleAdminDTO.booked());
        eventSchedule.setMaxCapacity(eventScheduleAdminDTO.maxCapacity());
        eventSchedule.setAvailableSpots(eventScheduleAdminDTO.availableSpots());
    }
}

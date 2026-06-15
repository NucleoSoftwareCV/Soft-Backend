package com.hean.consigueventas.oonabe.eventSchedule.controller;

import com.hean.consigueventas.oonabe.eventSchedule.dto.EventScheduleDTO;
import com.hean.consigueventas.oonabe.eventSchedule.service.EventScheduleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/event-schedules")
public class EventScheduleController {

    private final EventScheduleService eventScheduleService;

    public EventScheduleController(EventScheduleService eventScheduleService) {
        this.eventScheduleService = eventScheduleService;
    }

    @GetMapping
    public List<EventScheduleDTO> getAllSchedules() {
        return eventScheduleService.getAllSchedules();
    }

    //Listar fecha por id
    @GetMapping("/{id}")
    public EventScheduleDTO getScheduleById(@PathVariable Long id) {
        return eventScheduleService.getScheduleById(id);
    }

    //Crear fecha
    @PostMapping
    public EventScheduleDTO createSchedule(
            @Valid @RequestBody EventScheduleDTO eventScheduleDTO
    ) {
        return eventScheduleService.createSchedule(eventScheduleDTO);
    }

    //Editar fecha por id
    @PutMapping("/{id}")
    public EventScheduleDTO updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody EventScheduleDTO eventScheduleDTO
    ) {
        return eventScheduleService.updateSchedule(id, eventScheduleDTO);
    }

    //Eliminar fecha por id
    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable Long id) {
        eventScheduleService.deleteSchedule(id);
    }

    //Listar filtro de fecha por dia y horario
    @GetMapping("/filter")
    public List<EventScheduleDTO> filterSchedules(
            @RequestParam(required = false) String dateFilter,

            @RequestParam(required = false) String timeFilter,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate selectedDate
    ) {
        return eventScheduleService.filterSchedules(
                dateFilter,
                timeFilter,
                selectedDate
        );
    }
}
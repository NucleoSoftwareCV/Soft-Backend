package com.hean.consigueventas.oonabe.eventSchedule.controller;

import com.hean.consigueventas.oonabe.eventSchedule.dto.EventScheduleAdminDTO;
import com.hean.consigueventas.oonabe.eventSchedule.dto.EventScheduleUsuarioDTO;
import com.hean.consigueventas.oonabe.eventSchedule.service.EventScheduleService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/event-schedules")
public class EventScheduleController {

    private final EventScheduleService eventScheduleService;

    public EventScheduleController(EventScheduleService eventScheduleService) {
        this.eventScheduleService = eventScheduleService;
    }

    //Vista administrativa: muestra todos los horarios registrados para el admin
    @GetMapping
    public List<EventScheduleAdminDTO> getAllSchedules() {
        return eventScheduleService.getAllSchedules();
    }

    //Vista publica: muestra todos los horarios registrados para el usuario
    @GetMapping("/public")
    public List<EventScheduleUsuarioDTO> getPublicSchedules() {
        return eventScheduleService.getPublicSchedules();
    }

    //Vista publica: filtra los horarios visibles para usuarios segun una fecha exacta
    @GetMapping("/public/date")
    public List<EventScheduleUsuarioDTO> getPublicSchedulesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return eventScheduleService.getPublicSchedulesByDate(date);
    }

    //Vista publica: filtra los horarios visibles para usuarios por  un rango de fechas
    @GetMapping("/public/range")
    public List<EventScheduleUsuarioDTO> getPublicSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return eventScheduleService.getPublicSchedulesByDateRange(startDate, endDate);
    }

    //Vista administrativa: filtra los horarios por ID
    @GetMapping("/{id}")
    public EventScheduleAdminDTO getScheduleById(@PathVariable Long id) {
        return eventScheduleService.getScheduleById(id);
    }

    //Vista administrativa: crea un nuevo horario con los campos para admin
    @PostMapping
    public EventScheduleAdminDTO createSchedule(
            @Valid @RequestBody EventScheduleAdminDTO eventScheduleAdminDTO
    ) {
        return eventScheduleService.createSchedule(eventScheduleAdminDTO);
    }

    //Vista administrativa: actualiza un horario existente por su ID
    @PutMapping("/{id}")
    public EventScheduleAdminDTO updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody EventScheduleAdminDTO eventScheduleAdminDTO
    ) {
        return eventScheduleService.updateSchedule(id, eventScheduleAdminDTO);
    }

    //Vista administrativa: elimina un horario  por ID cuando ya no debe estar disponible.
    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable Long id) {
        eventScheduleService.deleteSchedule(id);
    }

    //Vista administrativa: aplica filtros combinados por fecha y  horas
    @GetMapping("/filter")
    public List<EventScheduleAdminDTO> filterSchedules(
            @Valid
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
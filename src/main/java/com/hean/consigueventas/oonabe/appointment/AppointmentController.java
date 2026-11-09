package com.hean.consigueventas.oonabe.appointment;

import static com.hean.consigueventas.oonabe.appointment.AppointmentDtos.*;
import static com.hean.consigueventas.oonabe.common.security.SecurityUtils.getAuthenticatedUserId;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) { this.service=service; }
    private <T> ResponseEntity<T> privateResponse(T body) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body); }
    @GetMapping("/appointments/services/{id}") @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Options> options(@PathVariable Long id) { return privateResponse(service.options(id)); }
    @GetMapping("/appointments/services/{id}/availability") @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Availability> availability(@PathVariable Long id, @RequestParam LocalDate date,
            @RequestParam SessionModality modality, @RequestParam(required=false) Long locationId) {
        return privateResponse(service.availability(id,date,modality,locationId));
    }
    @GetMapping("/appointments/requests/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Receipt>> myAppointments() {
        return privateResponse(service.myAppointments(getAuthenticatedUserId()));
    }

    @PatchMapping("/appointments/requests/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Receipt> cancel(@PathVariable Long id, @Valid @RequestBody Cancellation input) {
        return privateResponse(service.cancelMyAppointment(getAuthenticatedUserId(), id, input.reason()));
    }

    @PatchMapping("/appointments/requests/{id}/reschedule")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Receipt> reschedule(@PathVariable Long id, @Valid @RequestBody Reschedule input) {
        return privateResponse(service.rescheduleMyAppointment(getAuthenticatedUserId(), id, input));
    }

    @PostMapping("/appointments/requests") @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Receipt> request(@Valid @RequestBody Request input) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(service.request(getAuthenticatedUserId(),input));
    }
    @GetMapping("/professional-agenda/availability") @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<List<Rule>> rules() { return privateResponse(service.rules(getAuthenticatedUserId())); }
    @PostMapping("/professional-agenda/availability") @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<Rule> rule(@Valid @RequestBody RuleInput input) { return privateResponse(service.addRule(getAuthenticatedUserId(),input)); }
    @DeleteMapping("/professional-agenda/availability/{id}") @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<Void> remove(@PathVariable Long id) { service.removeRule(getAuthenticatedUserId(),id); return ResponseEntity.noContent().build(); }
    @GetMapping("/professional-agenda/requests") @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<List<Receipt>> agenda(@RequestParam LocalDate date) { return privateResponse(service.agenda(getAuthenticatedUserId(),date)); }
    @GetMapping("/professional-agenda/requests/range")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<List<Receipt>> agendaRange(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return privateResponse(service.agendaRange(getAuthenticatedUserId(), from, to));
    }

    @GetMapping("/professional-agenda/requests/pending")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<List<Receipt>> pending() {
        return privateResponse(service.pending(getAuthenticatedUserId()));
    }

    @PatchMapping("/professional-agenda/requests/{id}") @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<Receipt> decide(@PathVariable Long id,@Valid @RequestBody Decision input) { return privateResponse(service.decide(getAuthenticatedUserId(),id,input.status(),input.reason())); }
}

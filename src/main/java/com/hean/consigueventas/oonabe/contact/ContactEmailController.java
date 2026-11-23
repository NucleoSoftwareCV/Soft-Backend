package com.hean.consigueventas.oonabe.contact;



import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "http://localhost:4200")
public class ContactEmailController {

    private final ContactEmailService contactEmailService;

    public ContactEmailController(
            ContactEmailService contactEmailService
    ) {
        this.contactEmailService = contactEmailService;
    }

    @PostMapping("/email")
    public ResponseEntity<Void> enviarEmail(
            @Valid @RequestBody ContactEmailRequest request
    ) {

        contactEmailService.enviarSolicitud(request);

        return ResponseEntity.ok().build();
    }
}
package com.hean.consigueventas.oonabe.profileCliente.controller;

import com.hean.consigueventas.oonabe.profileCliente.dto.request.ClientProfilePreferencesRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientProfilePreferencesResponse;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientSpaceResponse;
import com.hean.consigueventas.oonabe.profileCliente.service.ClientProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/client-profiles")
@Tag(
        name = "Perfiles cliente",
        description = "Gestión del perfil cliente, preferencias, ciudad, intereses y comunicaciones."
)
public class ClientProfileController {

    private final ClientProfileService clientProfileService;

    public ClientProfileController(
            ClientProfileService clientProfileService
    ) {
        this.clientProfileService = clientProfileService;
    }

    //Cliente: consultar su espacio principal
    @GetMapping("/me/space")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Consultar mi espacio",
            description = "Devuelve la información principal del espacio del cliente autenticado."
    )
    public ClientSpaceResponse getMySpace(
            Authentication authentication
    ) {
        return clientProfileService.getMySpace(
                authentication.getName()
        );
    }

    //Cliente: consultar sus preferencias
    @GetMapping("/me/preferences")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Consultar mis preferencias",
            description = "Devuelve las preferencias del cliente autenticado, incluyendo ciudad, categorías de interés y comunicaciones."
    )
    public ClientProfilePreferencesResponse getMyPreferences(
            Authentication authentication
    ) {
        return clientProfileService.getMyPreferences(
                authentication.getName()
        );
    }

    //Cliente: guardar sus preferencias
    @PutMapping("/me/preferences")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Guardar mis preferencias",
            description = "Permite al cliente actualizar sus datos personales, ciudad, categorías de interés y preferencias de comunicación."
    )
    public ClientProfilePreferencesResponse saveMyPreferences(
            Authentication authentication,
            @Valid @RequestBody ClientProfilePreferencesRequest request
    ) {
        return clientProfileService.saveMyPreferences(
                authentication.getName(),
                request
        );
    }
}
package com.hean.consigueventas.oonabe.profileCliente.controller;

import com.hean.consigueventas.oonabe.profileCliente.dto.request.ClientProfilePreferencesRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.OnboardingInterestsRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.OnboardingPreferencesRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.OnboardingStatusRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientOnboardingResponse;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientProfilePreferencesResponse;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientSpaceResponse;
import com.hean.consigueventas.oonabe.profileCliente.service.ClientProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @GetMapping("/me/onboarding")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Consultar mi onboarding",
            description = "Devuelve el progreso y las preferencias elegidas por el cliente autenticado.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponse(responseCode = "200", description = "Estado del onboarding")
    public ClientOnboardingResponse getMyOnboarding(Authentication authentication) {
        return clientProfileService.getMyOnboarding(authentication.getName());
    }

    @PutMapping("/me/onboarding/interests")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Guardar intereses iniciales",
            description = "Reemplaza las categorías de interés elegidas en el primer paso.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponse(responseCode = "200", description = "Intereses guardados")
    @ApiResponse(responseCode = "400", description = "Debe seleccionar al menos una categoría")
    public ClientOnboardingResponse saveOnboardingInterests(
            Authentication authentication,
            @Valid @RequestBody OnboardingInterestsRequest request
    ) {
        return clientProfileService.saveOnboardingInterests(authentication.getName(), request);
    }

    @PutMapping("/me/onboarding/preferences")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Guardar preferencias ampliadas",
            description = "Guarda ciudad, tipos de experiencia y modalidad; todos son opcionales.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponse(responseCode = "200", description = "Preferencias guardadas y onboarding completado")
    @ApiResponse(responseCode = "404", description = "Algún catálogo activo no existe")
    public ClientOnboardingResponse saveOnboardingPreferences(
            Authentication authentication,
            @Valid @RequestBody OnboardingPreferencesRequest request
    ) {
        return clientProfileService.saveOnboardingPreferences(authentication.getName(), request);
    }

    @PatchMapping("/me/onboarding/status")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Finalizar u omitir onboarding",
            description = "Marca el onboarding como completado u omitido sin eliminar selecciones previas.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado no permitido")
    public ClientOnboardingResponse updateOnboardingStatus(
            Authentication authentication,
            @Valid @RequestBody OnboardingStatusRequest request
    ) {
        return clientProfileService.updateOnboardingStatus(authentication.getName(), request.status());
    }
}

package com.hean.consigueventas.oonabe.interaction.controller;

import com.hean.consigueventas.oonabe.interaction.dto.response.EventFavoriteResponse;
import com.hean.consigueventas.oonabe.interaction.dto.response.EventFavoriteStatusResponse;
import com.hean.consigueventas.oonabe.interaction.dto.response.FollowedProfessionalResponse;
import com.hean.consigueventas.oonabe.interaction.dto.response.ProfessionalFollowStatusResponse;
import com.hean.consigueventas.oonabe.interaction.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interactions")
@Tag(
        name = "Interacciones",
        description = "Gestión de eventos favoritos y profesionales seguidos por el cliente."
)
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(
            InteractionService interactionService
    ) {
        this.interactionService = interactionService;
    }

    //Cliente: guarda un evento como favorito
    @PutMapping("/me/event-favorites/{eventId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Guardar evento como favorito",
            description = "Permite al cliente autenticado guardar un evento como favorito."
    )
    public EventFavoriteStatusResponse saveEventAsFavorite(
            Authentication authentication,
            @PathVariable Long eventId
    ) {
        return interactionService.saveEventAsFavorite(
                authentication.getName(),
                eventId
        );
    }

    //Cliente: elimina un evento de favoritos
    @DeleteMapping("/me/event-favorites/{eventId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Eliminar evento de favoritos",
            description = "Permite al cliente autenticado quitar un evento de su lista de favoritos."
    )
    public EventFavoriteStatusResponse removeEventFromFavorites(
            Authentication authentication,
            @PathVariable Long eventId
    ) {
        return interactionService.removeEventFromFavorites(
                authentication.getName(),
                eventId
        );
    }

    //Cliente: consulta si un evento está marcado como favorito
    @GetMapping("/me/event-favorites/{eventId}/status")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Consultar estado de favorito",
            description = "Indica si el cliente autenticado ya guardó un evento como favorito."
    )
    public EventFavoriteStatusResponse getEventFavoriteStatus(
            Authentication authentication,
            @PathVariable Long eventId
    ) {
        return interactionService.getEventFavoriteStatus(
                authentication.getName(),
                eventId
        );
    }

    //Cliente: lista sus eventos favoritos
    @GetMapping("/me/event-favorites")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Listar mis eventos favoritos",
            description = "Devuelve los eventos guardados como favoritos por el cliente autenticado."
    )
    public List<EventFavoriteResponse> getMyFavoriteEvents(
            Authentication authentication
    ) {
        return interactionService.getMyFavoriteEvents(
                authentication.getName()
        );
    }

    //Cliente: sigue a un profesional
    @PutMapping("/me/professional-follows/{professionalId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Seguir profesional",
            description = "Permite al cliente autenticado seguir un perfil profesional público."
    )
    public ProfessionalFollowStatusResponse followProfessional(
            Authentication authentication,
            @PathVariable Long professionalId
    ) {
        return interactionService.followProfessional(
                authentication.getName(),
                professionalId
        );
    }

    //Cliente: deja de seguir a un profesional
    @DeleteMapping("/me/professional-follows/{professionalId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Dejar de seguir profesional",
            description = "Permite al cliente autenticado dejar de seguir un perfil profesional."
    )
    public ProfessionalFollowStatusResponse unfollowProfessional(
            Authentication authentication,
            @PathVariable Long professionalId
    ) {
        return interactionService.unfollowProfessional(
                authentication.getName(),
                professionalId
        );
    }

    //Cliente: consulta si sigue a un profesional
    @GetMapping("/me/professional-follows/{professionalId}/status")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Consultar estado de seguimiento",
            description = "Indica si el cliente autenticado sigue a un perfil profesional."
    )
    public ProfessionalFollowStatusResponse getProfessionalFollowStatus(
            Authentication authentication,
            @PathVariable Long professionalId
    ) {
        return interactionService.getProfessionalFollowStatus(
                authentication.getName(),
                professionalId
        );
    }

    //Cliente: lista los profesionales que sigue
    @GetMapping("/me/professional-follows")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Listar profesionales seguidos",
            description = "Devuelve los perfiles profesionales seguidos por el cliente autenticado."
    )
    public Page<FollowedProfessionalResponse> getMyFollowedProfessionals(
            Authentication authentication,

            @ParameterObject
            @PageableDefault(
                    size = 12,
                    sort = "followedAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return interactionService.getMyFollowedProfessionals(
                authentication.getName(),
                pageable
        );
    }
}
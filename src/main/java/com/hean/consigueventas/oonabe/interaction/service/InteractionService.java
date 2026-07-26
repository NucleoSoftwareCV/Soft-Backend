package com.hean.consigueventas.oonabe.interaction.service;

import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.interaction.dto.response.EventFavoriteResponse;
import com.hean.consigueventas.oonabe.interaction.dto.response.EventFavoriteStatusResponse;
import com.hean.consigueventas.oonabe.interaction.dto.response.FollowedProfessionalResponse;
import com.hean.consigueventas.oonabe.interaction.dto.response.ProfessionalFollowStatusResponse;
import com.hean.consigueventas.oonabe.interaction.entity.EventFavorite;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import com.hean.consigueventas.oonabe.interaction.mapper.InteractionMapper;
import com.hean.consigueventas.oonabe.interaction.repository.EventFavoriteRepository;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final EventFavoriteRepository eventFavoriteRepository;
    private final ProfessionalFollowRepository professionalFollowRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final EventRepository eventRepository;
    private final SpecialistProfileRepository specialistProfileRepository;
    private final UserRepository userRepository;
    private final InteractionMapper interactionMapper;

    @Transactional
    public EventFavoriteStatusResponse saveEventAsFavorite(
            String username,
            Long eventId
    ) {
        User user = getUserByUsername(username);
        ClientProfile clientProfile = getOrCreateClientProfile(user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento no encontrado."
                        )
                );

        boolean alreadyFavorite =
                eventFavoriteRepository.existsByClientProfileIdAndEventId(
                        clientProfile.getId(),
                        event.getId()
                );

        if (!alreadyFavorite) {
            EventFavorite eventFavorite = new EventFavorite();

            eventFavorite.setClientProfile(clientProfile);
            eventFavorite.setEvent(event);

            eventFavoriteRepository.save(eventFavorite);
        }

        return new EventFavoriteStatusResponse(
                eventId,
                true
        );
    }

    @Transactional
    public EventFavoriteStatusResponse removeEventFromFavorites(
            String username,
            Long eventId
    ) {
        User user = getUserByUsername(username);
        ClientProfile clientProfile = getOrCreateClientProfile(user);

        eventFavoriteRepository.deleteByClientProfileIdAndEventId(
                clientProfile.getId(),
                eventId
        );

        return new EventFavoriteStatusResponse(
                eventId,
                false
        );
    }

    @Transactional(readOnly = true)
    public EventFavoriteStatusResponse getEventFavoriteStatus(
            String username,
            Long eventId
    ) {
        User user = getUserByUsername(username);
        ClientProfile clientProfile = getOrCreateClientProfile(user);

        boolean favorite =
                eventFavoriteRepository.existsByClientProfileIdAndEventId(
                        clientProfile.getId(),
                        eventId
                );

        return new EventFavoriteStatusResponse(
                eventId,
                favorite
        );
    }

    @Transactional(readOnly = true)
    public List<EventFavoriteResponse> getMyFavoriteEvents(
            String username
    ) {
        User user = getUserByUsername(username);
        ClientProfile clientProfile = getOrCreateClientProfile(user);

        return eventFavoriteRepository
                .findByClientProfileIdOrderByCreatedAtDesc(
                        clientProfile.getId()
                )
                .stream()
                .map(interactionMapper::toEventFavoriteResponse)
                .toList();
    }

    @Transactional
    public ProfessionalFollowStatusResponse followProfessional(
            String username,
            Long professionalId
    ) {
        User user = getUserByUsername(username);
        ClientProfile clientProfile = getOrCreateClientProfile(user);

        SpecialistProfile specialistProfile =
                getPublicSpecialistProfile(professionalId);

        if (
                specialistProfile.getUser() != null
                        && specialistProfile.getUser().getId().equals(user.getId())
        ) {
            throw new IllegalStateException(
                    "No puedes seguir tu propio perfil profesional."
            );
        }

        boolean alreadyFollowing =
                professionalFollowRepository
                        .existsByClientProfileIdAndSpecialistProfileId(
                                clientProfile.getId(),
                                specialistProfile.getId()
                        );

        if (!alreadyFollowing) {
            ProfessionalFollow professionalFollow =
                    new ProfessionalFollow();

            professionalFollow.setClientProfile(clientProfile);
            professionalFollow.setSpecialistProfile(specialistProfile);

            professionalFollowRepository.save(professionalFollow);
        }

        return buildProfessionalFollowStatus(
                specialistProfile.getId(),
                true
        );
    }

    @Transactional
    public ProfessionalFollowStatusResponse unfollowProfessional(
            String username,
            Long professionalId
    ) {
        User user = getUserByUsername(username);
        ClientProfile clientProfile = getOrCreateClientProfile(user);

        professionalFollowRepository
                .deleteByClientProfileIdAndSpecialistProfileId(
                        clientProfile.getId(),
                        professionalId
                );

        return buildProfessionalFollowStatus(
                professionalId,
                false
        );
    }

    @Transactional(readOnly = true)
    public ProfessionalFollowStatusResponse getProfessionalFollowStatus(
            String username,
            Long professionalId
    ) {
        User user = getUserByUsername(username);
        ClientProfile clientProfile = getOrCreateClientProfile(user);

        getPublicSpecialistProfile(professionalId);

        boolean following =
                professionalFollowRepository
                        .existsByClientProfileIdAndSpecialistProfileId(
                                clientProfile.getId(),
                                professionalId
                        );

        return buildProfessionalFollowStatus(
                professionalId,
                following
        );
    }

    @Transactional(readOnly = true)
    public Page<FollowedProfessionalResponse> getMyFollowedProfessionals(
            String username,
            Pageable pageable
    ) {
        User user = getUserByUsername(username);
        ClientProfile clientProfile = getOrCreateClientProfile(user);

        return professionalFollowRepository
                .findByClientProfileId(
                        clientProfile.getId(),
                        pageable
                )
                .map(interactionMapper::toFollowedProfessionalResponse);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario autenticado no encontrado."
                        )
                );
    }

    private ClientProfile getOrCreateClientProfile(User user) {
        return clientProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ClientProfile clientProfile = new ClientProfile();

                    clientProfile.setUser(user);
                    clientProfile.setCommunicationEmail(user.getEmail());

                    return clientProfileRepository.save(clientProfile);
                });
    }

    private SpecialistProfile getPublicSpecialistProfile(
            Long professionalId
    ) {
        SpecialistProfile specialistProfile =
                specialistProfileRepository.findById(professionalId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Perfil profesional no encontrado."
                                )
                        );

        if (
                specialistProfile.getApprovalStatus() != ApprovalStatus.APROBADO
                        || specialistProfile.getPublicationStatus()
                        != PublicationStatus.PUBLICADO
        ) {
            throw new ResourceNotFoundException(
                    "Perfil profesional no disponible públicamente."
            );
        }

        return specialistProfile;
    }

    private ProfessionalFollowStatusResponse buildProfessionalFollowStatus(
            Long professionalId,
            Boolean following
    ) {
        Long followersCount =
                professionalFollowRepository
                        .countBySpecialistProfileId(professionalId);

        return new ProfessionalFollowStatusResponse(
                professionalId,
                following,
                followersCount
        );
    }
}
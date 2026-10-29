package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.interaction.repository.EventFavoriteRepository;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientModalityPreference;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientEventTypePreferenceRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientInterestCategoryRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientModalityPreferenceRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationSignalsService {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ClientInterestCategoryRepository interestCategoryRepository;
    private final ClientEventTypePreferenceRepository eventTypePreferenceRepository;
    private final ClientModalityPreferenceRepository modalityPreferenceRepository;
    private final EventFavoriteRepository eventFavoriteRepository;
    private final ProfessionalFollowRepository professionalFollowRepository;

    public RecommendationSignals getSignals(String username) {
        Objects.requireNonNull(username, "El usuario es obligatorio");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario autenticado no encontrado."
                ));

        return clientProfileRepository.findByUserId(user.getId())
                .map(this::buildSignals)
                .orElseGet(this::emptySignals);
    }

    private RecommendationSignals buildSignals(ClientProfile profile) {
        Long profileId = profile.getId();

        Set<Long> interestCategoryIds =
                interestCategoryRepository.findByClientProfileId(profileId)
                        .stream()
                        .map(interest -> interest.getCategory().getId())
                        .collect(Collectors.toSet());

        Set<Long> favoriteCategoryIds = new HashSet<>(
                eventFavoriteRepository
                        .findFavoriteCategoryIdsByClientProfileId(profileId)
        );

        Set<Long> preferredExperienceTypeIds =
                eventTypePreferenceRepository.findByClientProfileId(profileId)
                        .stream()
                        .map(preference -> preference.getExperienceType().getId())
                        .collect(Collectors.toSet());

        Set<Long> followedSpecialistIds = new HashSet<>(
                professionalFollowRepository
                        .findFollowedSpecialistIdsByClientProfileId(profileId)
        );

        EventModality preferredModality =
                modalityPreferenceRepository.findByClientProfileId(profileId)
                        .map(ClientModalityPreference::getModality)
                        .orElse(null);

        String cityName = profile.getCity() == null
                ? null
                : profile.getCity().getName();

        return new RecommendationSignals(
                interestCategoryIds,
                favoriteCategoryIds,
                preferredExperienceTypeIds,
                followedSpecialistIds,
                preferredModality,
                cityName
        );
    }

    private RecommendationSignals emptySignals() {
        return new RecommendationSignals(
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                null,
                null
        );
    }
}
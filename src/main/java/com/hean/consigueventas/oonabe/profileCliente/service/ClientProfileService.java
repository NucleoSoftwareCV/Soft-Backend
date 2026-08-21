package com.hean.consigueventas.oonabe.profileCliente.service;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
import com.hean.consigueventas.oonabe.interaction.repository.EventFavoriteRepository;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.ClientProfilePreferencesRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.OnboardingInterestsRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.OnboardingPreferencesRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientProfilePreferencesResponse;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientOnboardingResponse;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientSpaceResponse;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientInterestCategory;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientEventTypePreference;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientModalityPreference;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.entity.OnboardingStatus;
import com.hean.consigueventas.oonabe.profileCliente.mapper.ClientProfileMapper;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientInterestCategoryRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientEventTypePreferenceRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientModalityPreferenceRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class ClientProfileService {

    private final ClientProfileRepository clientProfileRepository;
    private final ClientInterestCategoryRepository clientInterestCategoryRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final ClientProfileMapper clientProfileMapper;
    private final EventFavoriteRepository eventFavoriteRepository;
    private final ProfessionalFollowRepository professionalFollowRepository;
    private final ExperienceTypeRepository experienceTypeRepository;
    private final ClientEventTypePreferenceRepository clientEventTypePreferenceRepository;
    private final ClientModalityPreferenceRepository clientModalityPreferenceRepository;

    @Transactional
    public ClientProfilePreferencesResponse getMyPreferences(
            String username
    ) {
        User user = getUserByUsername(username);

        ClientProfile clientProfile =
                clientProfileRepository.findByUserId(user.getId())
                        .orElseGet(() -> {
                            ClientProfile newClientProfile =
                                    createDefaultClientProfile(user);

                            return clientProfileRepository.save(
                                    newClientProfile
                            );
                        });

        return buildPreferencesResponse(clientProfile);
    }

    @Transactional
    public ClientProfilePreferencesResponse saveMyPreferences(
            String username,
            ClientProfilePreferencesRequest request
    ) {
        User user = getUserByUsername(username);

        ClientProfile clientProfile =
                clientProfileRepository.findByUserId(user.getId())
                        .orElseGet(() -> createDefaultClientProfile(user));

        updateClientProfileData(clientProfile, request);

        ClientProfile savedClientProfile =
                clientProfileRepository.save(clientProfile);

        if (request.categoryIds() != null) {
            updateInterestCategories(
                    savedClientProfile,
                    request.categoryIds()
            );
        }

        return buildPreferencesResponse(savedClientProfile);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario autenticado no encontrado."
                        )
                );
    }

    private ClientProfile createDefaultClientProfile(User user) {
        ClientProfile clientProfile = new ClientProfile();

        clientProfile.setUser(user);
        clientProfile.setCommunicationEmail(user.getEmail());

        return clientProfile;
    }

    // Se llama al registrarse para que el nombre real quede disponible de inmediato
    // (en vez de esperar a que el usuario visite sus preferencias para completarlo).
    @Transactional
    public void createInitialProfile(User user, String firstName, String lastName) {
        if (clientProfileRepository.existsByUserId(user.getId())) {
            return;
        }

        ClientProfile clientProfile = createDefaultClientProfile(user);
        clientProfile.setFirstName(cleanText(firstName));
        clientProfile.setLastName(cleanText(lastName));
        clientProfile.setOnboardingStatus(OnboardingStatus.NOT_STARTED);
        clientProfileRepository.save(clientProfile);
    }

    @Transactional(readOnly = true)
    public ClientOnboardingResponse getMyOnboarding(String username) {
        return buildOnboardingResponse(getOrCreateClientProfile(username));
    }

    @Transactional
    public ClientOnboardingResponse saveOnboardingInterests(
            String username,
            OnboardingInterestsRequest request
    ) {
        ClientProfile profile = getOrCreateClientProfile(username);
        updateInterestCategories(profile, request.categoryIds());
        profile.setOnboardingStatus(OnboardingStatus.INTERESTS_SAVED);
        clientProfileRepository.save(profile);
        return buildOnboardingResponse(profile);
    }

    @Transactional
    public ClientOnboardingResponse saveOnboardingPreferences(
            String username,
            OnboardingPreferencesRequest request
    ) {
        ClientProfile profile = getOrCreateClientProfile(username);
        updateOnboardingCity(profile, request.cityId());
        updateEventTypePreferences(profile, request.experienceTypeIds());
        updateModalityPreference(profile, request.modality());
        profile.setOnboardingStatus(OnboardingStatus.COMPLETED);
        clientProfileRepository.save(profile);
        return buildOnboardingResponse(profile);
    }

    @Transactional
    public ClientOnboardingResponse updateOnboardingStatus(
            String username,
            OnboardingStatus status
    ) {
        if (status != OnboardingStatus.SKIPPED && status != OnboardingStatus.COMPLETED) {
            throw new BusinessLogicException("Solo se puede omitir o completar el onboarding.");
        }
        ClientProfile profile = getOrCreateClientProfile(username);
        profile.setOnboardingStatus(status);
        clientProfileRepository.save(profile);
        return buildOnboardingResponse(profile);
    }

    private ClientProfile getOrCreateClientProfile(String username) {
        User user = getUserByUsername(username);
        return clientProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> clientProfileRepository.save(createDefaultClientProfile(user)));
    }

    private void updateOnboardingCity(ClientProfile profile, Long cityId) {
        if (cityId == null) {
            profile.setCity(null);
            return;
        }
        City city = cityRepository.findById(cityId)
                .filter(candidate -> Boolean.TRUE.equals(candidate.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Ciudad activa no encontrada."));
        profile.setCity(city);
    }

    private void updateEventTypePreferences(ClientProfile profile, Set<Long> ids) {
        clientEventTypePreferenceRepository.deleteByClientProfileId(profile.getId());
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<ExperienceType> types = experienceTypeRepository.findAllById(ids);
        if (types.size() != ids.size() || types.stream().anyMatch(type -> !type.isActive())) {
            throw new ResourceNotFoundException("Uno o más tipos de experiencia activos no existen.");
        }
        clientEventTypePreferenceRepository.saveAll(types.stream().map(type -> {
            ClientEventTypePreference preference = new ClientEventTypePreference();
            preference.setClientProfile(profile);
            preference.setExperienceType(type);
            return preference;
        }).toList());
    }

    private void updateModalityPreference(ClientProfile profile, EventModality modality) {
        ClientModalityPreference preference = clientModalityPreferenceRepository
                .findByClientProfileId(profile.getId())
                .orElseGet(ClientModalityPreference::new);
        if (modality == null) {
            if (preference.getId() != null) {
                clientModalityPreferenceRepository.delete(preference);
            }
            return;
        }
        preference.setClientProfile(profile);
        preference.setModality(modality);
        clientModalityPreferenceRepository.save(preference);
    }

    private ClientOnboardingResponse buildOnboardingResponse(ClientProfile profile) {
        Set<Long> categoryIds = clientInterestCategoryRepository.findByClientProfileId(profile.getId())
                .stream().map(relation -> relation.getCategory().getId())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<Long> experienceTypeIds = clientEventTypePreferenceRepository.findByClientProfileId(profile.getId())
                .stream().map(relation -> relation.getExperienceType().getId())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        EventModality modality = clientModalityPreferenceRepository.findByClientProfileId(profile.getId())
                .map(ClientModalityPreference::getModality).orElse(null);
        OnboardingStatus status = profile.getOnboardingStatus() == null
                ? OnboardingStatus.SKIPPED
                : profile.getOnboardingStatus();
        return new ClientOnboardingResponse(
                status,
                categoryIds,
                profile.getCity() == null ? null : profile.getCity().getId(),
                experienceTypeIds,
                modality
        );
    }

    private void updateClientProfileData(
            ClientProfile clientProfile,
            ClientProfilePreferencesRequest request
    ) {
        clientProfile.setFirstName(
                cleanText(request.firstName())
        );

        clientProfile.setLastName(
                cleanText(request.lastName())
        );

        clientProfile.setCommunicationEmail(
                cleanText(request.communicationEmail())
        );

        clientProfile.setWhatsappPhone(
                cleanText(request.whatsappPhone())
        );

        if (request.cityId() == null) {
            clientProfile.setCity(null);
        } else {
            City city = cityRepository.findById(request.cityId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Ciudad no encontrada."
                            )
                    );

            clientProfile.setCity(city);
        }

        if (request.receiveSavedEventConfirmations() != null) {
            clientProfile.setReceiveSavedEventConfirmations(
                    request.receiveSavedEventConfirmations()
            );
        }

        if (request.receivePersonalizedRecommendations() != null) {
            clientProfile.setReceivePersonalizedRecommendations(
                    request.receivePersonalizedRecommendations()
            );
        }

        if (request.receiveReservationConfirmations() != null) {
            clientProfile.setReceiveReservationConfirmations(
                    request.receiveReservationConfirmations()
            );
        }

        if (request.receiveWeeklySummary() != null) {
            clientProfile.setReceiveWeeklySummary(
                    request.receiveWeeklySummary()
            );
        }
    }

    private void updateInterestCategories(
            ClientProfile clientProfile,
            Set<Long> categoryIds
    ) {
        clientInterestCategoryRepository
                .deleteByClientProfileId(clientProfile.getId());

        if (categoryIds.isEmpty()) {
            return;
        }

        List<Category> categories =
                categoryRepository.findAllById(categoryIds);

        if (categories.size() != categoryIds.size()
                || categories.stream().anyMatch(category -> !category.isActive())) {
            throw new ResourceNotFoundException(
                    "Una o más categorías no existen."
            );
        }

        List<ClientInterestCategory> interestCategories =
                categories.stream()
                        .map(category -> {
                            ClientInterestCategory relation =
                                    new ClientInterestCategory();

                            relation.setClientProfile(clientProfile);
                            relation.setCategory(category);

                            return relation;
                        })
                        .toList();

        clientInterestCategoryRepository.saveAll(interestCategories);
    }

    private ClientProfilePreferencesResponse buildPreferencesResponse(
            ClientProfile clientProfile
    ) {
        List<ClientInterestCategory> interestCategories =
                clientInterestCategoryRepository
                        .findByClientProfileId(clientProfile.getId())
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        relation ->
                                                relation.getCategory().getName()
                                )
                        )
                        .toList();

        return clientProfileMapper.toPreferencesResponse(
                clientProfile,
                interestCategories
        );
    }

    private String cleanText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    @Transactional
    public ClientSpaceResponse getMySpace(
            String username
    ) {
        User user = getUserByUsername(username);

        ClientProfile clientProfile =
                clientProfileRepository.findByUserId(user.getId())
                        .orElseGet(() -> {
                            ClientProfile newClientProfile =
                                    createDefaultClientProfile(user);

                            return clientProfileRepository.save(
                                    newClientProfile
                            );
                        });

        Long favoriteEventCount =
                eventFavoriteRepository.countByClientProfileId(
                        clientProfile.getId()
                );

        Long followedProfessionalCount =
                professionalFollowRepository.countByClientProfileId(
                        clientProfile.getId()
                );

        return new ClientSpaceResponse(
                clientProfile.getId(),
                user.getId(),
                "Mi espacio",
                clientProfile.getFirstName(),
                clientProfile.getLastName(),
                "Tus reservas, guardados y profesionales en un solo lugar",
                0,
                0,
                favoriteEventCount,
                followedProfessionalCount
        );
    }
}

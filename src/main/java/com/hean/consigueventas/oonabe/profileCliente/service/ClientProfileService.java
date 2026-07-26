package com.hean.consigueventas.oonabe.profileCliente.service;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.interaction.repository.EventFavoriteRepository;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.ClientProfilePreferencesRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientProfilePreferencesResponse;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientSpaceResponse;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientInterestCategory;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.mapper.ClientProfileMapper;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientInterestCategoryRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

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

        if (categories.size() != categoryIds.size()) {
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
package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final String USERNAME = "client";
    private static final Long USER_ID = 42L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientProfileRepository clientProfileRepository;

    @Mock
    private RecommendationCandidateProvider candidateProvider;

    @InjectMocks
    private RecommendationService service;

    @AfterEach
    void verifiesThereAreNoAdditionalQueriesOrWrites() {
        verifyNoMoreInteractions(userRepository, clientProfileRepository, candidateProvider);
    }

    @Test
    void getsCandidatesForTheCityOfTheRequestedUser() {
        existingUser();
        profileWithCity("Valencia");
        Event candidate = event(1L);
        when(candidateProvider.findCandidates("Valencia")).thenReturn(List.of(candidate));

        assertThat(service.getRecommendations(USERNAME)).containsExactly(candidate);

        verifyProfileLookup();
        verify(candidateProvider).findCandidates("Valencia");
    }

    @Test
    void getsCandidatesWithoutCityWhenTheUserHasNoProfile() {
        existingUser();
        when(clientProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        Event candidate = event(2L);
        when(candidateProvider.findCandidates(null)).thenReturn(List.of(candidate));

        assertThat(service.getRecommendations(USERNAME)).containsExactly(candidate);

        verifyProfileLookup();
        verify(candidateProvider).findCandidates(null);
    }

    @Test
    void getsCandidatesWithoutCityWhenTheProfileHasNoCity() {
        existingUser();
        when(clientProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(new ClientProfile()));
        Event candidate = event(3L);
        when(candidateProvider.findCandidates(null)).thenReturn(List.of(candidate));

        assertThat(service.getRecommendations(USERNAME)).containsExactly(candidate);

        verifyProfileLookup();
        verify(candidateProvider).findCandidates(null);
    }

    @Test
    void rejectsAnUnknownUserWithoutLookingUpProfileOrCandidates() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRecommendations(USERNAME))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario autenticado no encontrado.");

        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void returnsAnEmptyListWhenTheProviderHasNoCandidates() {
        existingUser();
        profileWithCity("Valencia");
        when(candidateProvider.findCandidates("Valencia")).thenReturn(List.of());

        assertThat(service.getRecommendations(USERNAME)).isEmpty();

        verifyProfileLookup();
        verify(candidateProvider).findCandidates("Valencia");
    }

    @Test
    void preservesAllCandidatesAndTheirOriginalOrder() {
        existingUser();
        profileWithCity("Madrid");
        Event first = event(30L);
        Event second = event(10L);
        Event third = event(20L);
        when(candidateProvider.findCandidates("Madrid")).thenReturn(List.of(first, second, third));

        assertThat(service.getRecommendations(USERNAME)).containsExactly(first, second, third);

        verifyProfileLookup();
        verify(candidateProvider).findCandidates("Madrid");
    }

    @Test
    void delegatesCityNormalizationToTheCandidateProvider() {
        existingUser();
        profileWithCity("  vAlEnCiA  ");
        Event candidate = event(4L);
        when(candidateProvider.findCandidates("  vAlEnCiA  ")).thenReturn(List.of(candidate));

        assertThat(service.getRecommendations(USERNAME)).containsExactly(candidate);

        verifyProfileLookup();
        verify(candidateProvider).findCandidates("  vAlEnCiA  ");
    }

    private void existingUser() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
    }

    private void profileWithCity(String cityName) {
        City city = new City();
        city.setName(cityName);
        ClientProfile profile = new ClientProfile();
        profile.setId(99L);
        profile.setCity(city);
        when(clientProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
    }

    private void verifyProfileLookup() {
        verify(userRepository).findByUsername(USERNAME);
        verify(clientProfileRepository).findByUserId(USER_ID);
    }

    private Event event(Long id) {
        Event event = new Event();
        event.setId(id);
        return event;
    }
}

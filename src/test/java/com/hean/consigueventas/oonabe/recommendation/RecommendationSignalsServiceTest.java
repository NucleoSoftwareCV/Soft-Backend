package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.interaction.repository.EventFavoriteRepository;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientEventTypePreference;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientInterestCategory;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientModalityPreference;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientEventTypePreferenceRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientInterestCategoryRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientModalityPreferenceRepository;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationSignalsServiceTest {

    private static final String USERNAME = "cliente";
    private static final Long USER_ID = 7L;
    private static final Long PROFILE_ID = 100L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientProfileRepository clientProfileRepository;

    @Mock
    private ClientInterestCategoryRepository interestCategoryRepository;

    @Mock
    private ClientEventTypePreferenceRepository eventTypePreferenceRepository;

    @Mock
    private ClientModalityPreferenceRepository modalityPreferenceRepository;

    @Mock
    private EventFavoriteRepository eventFavoriteRepository;

    @Mock
    private ProfessionalFollowRepository professionalFollowRepository;

    @InjectMocks
    private RecommendationSignalsService service;

    private User user;
    private ClientProfile profile;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);

        profile = new ClientProfile();
        profile.setId(PROFILE_ID);
        profile.setUser(user);
    }

    @Test
    void shouldBuildSignalsFromStoredRelations() {
        mockUserAndProfile();

        Category category = new Category();
        category.setId(10L);

        ClientInterestCategory interest = new ClientInterestCategory();
        interest.setCategory(category);

        ExperienceType experienceType = new ExperienceType();
        experienceType.setId(20L);

        ClientEventTypePreference experiencePreference =
                new ClientEventTypePreference();
        experiencePreference.setExperienceType(experienceType);

        ClientModalityPreference modalityPreference =
                new ClientModalityPreference();
        modalityPreference.setModality(EventModality.ONLINE);

        City city = new City();
        city.setName("Lima");
        profile.setCity(city);

        when(interestCategoryRepository.findByClientProfileId(PROFILE_ID))
                .thenReturn(List.of(interest, interest));

        when(eventFavoriteRepository
                .findFavoriteCategoryIdsByClientProfileId(PROFILE_ID))
                .thenReturn(List.of(15L, 15L));

        when(eventTypePreferenceRepository.findByClientProfileId(PROFILE_ID))
                .thenReturn(List.of(experiencePreference));

        when(professionalFollowRepository
                .findFollowedSpecialistIdsByClientProfileId(PROFILE_ID))
                .thenReturn(List.of(30L, 30L));

        when(modalityPreferenceRepository.findByClientProfileId(PROFILE_ID))
                .thenReturn(Optional.of(modalityPreference));

        RecommendationSignals result = service.getSignals(USERNAME);

        assertAll(
                () -> assertEquals(Set.of(10L), result.interestCategoryIds()),
                () -> assertEquals(Set.of(15L), result.favoriteCategoryIds()),
                () -> assertEquals(
                        Set.of(20L), result.preferredExperienceTypeIds()
                ),
                () -> assertEquals(
                        Set.of(30L), result.followedSpecialistIds()
                ),
                () -> assertEquals(
                        EventModality.ONLINE, result.preferredModality()
                ),
                () -> assertEquals("Lima", result.cityName())
        );
    }

    @Test
    void shouldReturnEmptySignalsWhenProfileDoesNotExist() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(user));

        when(clientProfileRepository.findByUserId(USER_ID))
                .thenReturn(Optional.empty());

        assertEmptySignals(service.getSignals(USERNAME));

        verifyNoInteractions(
                interestCategoryRepository,
                eventFavoriteRepository,
                eventTypePreferenceRepository,
                professionalFollowRepository,
                modalityPreferenceRepository
        );

        verify(clientProfileRepository, never()).save(any());
    }

    @Test
    void shouldHandleProfileWithoutPreferencesOrInteractions() {
        mockUserAndProfile();

        when(interestCategoryRepository.findByClientProfileId(PROFILE_ID))
                .thenReturn(List.of());

        when(eventFavoriteRepository
                .findFavoriteCategoryIdsByClientProfileId(PROFILE_ID))
                .thenReturn(List.of());

        when(eventTypePreferenceRepository.findByClientProfileId(PROFILE_ID))
                .thenReturn(List.of());

        when(professionalFollowRepository
                .findFollowedSpecialistIdsByClientProfileId(PROFILE_ID))
                .thenReturn(List.of());

        when(modalityPreferenceRepository.findByClientProfileId(PROFILE_ID))
                .thenReturn(Optional.empty());

        assertEmptySignals(service.getSignals(USERNAME));
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getSignals(USERNAME)
        );

        verifyNoInteractions(
                clientProfileRepository,
                interestCategoryRepository,
                eventFavoriteRepository,
                eventTypePreferenceRepository,
                professionalFollowRepository,
                modalityPreferenceRepository
        );
    }

    @Test
    void shouldRejectNullUsername() {
        assertThrows(
                NullPointerException.class,
                () -> service.getSignals(null)
        );

        verifyNoInteractions(userRepository);
    }

    private void mockUserAndProfile() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(user));

        when(clientProfileRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(profile));
    }

    private void assertEmptySignals(RecommendationSignals result) {
        assertAll(
                () -> assertTrue(result.interestCategoryIds().isEmpty()),
                () -> assertTrue(result.favoriteCategoryIds().isEmpty()),
                () -> assertTrue(
                        result.preferredExperienceTypeIds().isEmpty()
                ),
                () -> assertTrue(result.followedSpecialistIds().isEmpty()),
                () -> assertNull(result.preferredModality()),
                () -> assertNull(result.cityName())
        );
    }
}
package com.hean.consigueventas.oonabe.profileCliente;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.ClientProfilePreferencesRequest;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.OnboardingInterestsRequest;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.mapper.ClientProfileMapper;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientInterestCategoryRepository;
import com.hean.consigueventas.oonabe.profileCliente.service.ClientProfileService;
import com.hean.consigueventas.oonabe.recommendation.RecommendationSignalsService;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.profileCliente.dto.request.OnboardingPreferencesRequest;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:preferences_categories;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import({
        ClientProfileService.class,
        ClientProfileMapper.class,
        RecommendationSignalsService.class
})
class ClientPreferencesCategoriesIntegrationTest {

    private static final String USERNAME = "preferences_test";

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientProfileService profileService;

    @Autowired
    private RecommendationSignalsService signalsService;

    @Autowired
    private ClientInterestCategoryRepository interestRepository;

    private Long profileId;
    private Long cityId;
    private Long yogaId;
    private Long movementId;
    private Long soundId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setEmail("preferences@example.com");
        entityManager.persist(user);

        City city = new City();
        city.setName("Valencia");
        city.setIsActive(true);
        entityManager.persist(city);
        cityId = city.getId();

        ClientProfile profile = new ClientProfile();
        profile.setUser(user);
        profile.setCity(city);
        profile.setCommunicationEmail(user.getEmail());
        entityManager.persist(profile);
        profileId = profile.getId();

        yogaId = createCategory("Yoga").getId();
        movementId = createCategory("Movimiento").getId();
        soundId = createCategory("Sonido").getId();

        flushAndClear();
    }


    @Test
    void referenceLocationPersistsChangesAndDoesNotLeakToAnotherAccount() {
        var initial = new com.hean.consigueventas.oonabe.profileCliente.dto.request.ReferenceLocationRequest("Mi barrio", 40.0, -3.0, true);
        profileService.saveReferenceLocation(USERNAME, initial);
        flushAndClear();
        assertThat(profileService.getMyPreferences(USERNAME).referenceLocation().label()).isEqualTo("Mi barrio");
        profileService.saveReferenceLocation(USERNAME,
                new com.hean.consigueventas.oonabe.profileCliente.dto.request.ReferenceLocationRequest("Otro punto", 39.0, 0.0, false));
        savePreferences(Set.of(yogaId));
        flushAndClear();
        assertThat(profileService.getReferenceLocation(USERNAME).latitude()).isEqualTo(39.0);
        User other = new User(); other.setUsername("other_reference"); other.setEmail("other-reference@example.com");
        entityManager.persist(other); flushAndClear();
        assertThat(profileService.getReferenceLocation("other_reference")).isNull();
        profileService.clearReferenceLocation(USERNAME); flushAndClear();
        assertThat(profileService.getReferenceLocation(USERNAME)).isNull();
    }

    @Test
    void shouldSaveTheSameCategoriesRepeatedlyWithoutDuplicates() {
        Set<Long> selected = Set.of(yogaId, movementId);

        savePreferences(selected);
        savePreferences(selected);
        savePreferences(selected);

        assertThat(storedCategoryIds())
                .containsExactlyInAnyOrderElementsOf(selected);
    }

    @Test
    void shouldReplaceCategoriesWhenSelectionsOverlap() {
        savePreferences(Set.of(yogaId, movementId));

        savePreferences(Set.of(movementId, soundId));

        assertThat(storedCategoryIds())
                .containsExactlyInAnyOrder(movementId, soundId)
                .doesNotContain(yogaId);
    }

    @Test
    void shouldValidateCategoriesBeforeDeletingExistingRelations() {
        savePreferences(Set.of(yogaId));

        assertThrows(
                ResourceNotFoundException.class,
                () -> profileService.saveMyPreferences(
                        USERNAME,
                        request(Set.of(Long.MAX_VALUE))
                )
        );

        assertThat(storedCategoryIds())
                .containsExactly(yogaId);
    }

    @Test
    void shouldAllowRepeatedOnboardingInterestUpdates() {
        profileService.saveOnboardingInterests(
                USERNAME,
                new OnboardingInterestsRequest(Set.of(yogaId, movementId))
        );
        flushAndClear();

        profileService.saveOnboardingInterests(
                USERNAME,
                new OnboardingInterestsRequest(Set.of(yogaId, movementId))
        );
        flushAndClear();

        profileService.saveOnboardingInterests(
                USERNAME,
                new OnboardingInterestsRequest(Set.of(movementId, soundId))
        );
        flushAndClear();

        assertThat(storedCategoryIds())
                .containsExactlyInAnyOrder(movementId, soundId);
    }

    @Test
    void shouldExposeUpdatedCategoriesToRecommendationSignals() {
        savePreferences(Set.of(yogaId));

        assertThat(signalsService.getSignals(USERNAME).interestCategoryIds())
                .containsExactly(yogaId);

        savePreferences(Set.of(movementId, soundId));

        assertThat(signalsService.getSignals(USERNAME).interestCategoryIds())
                .containsExactlyInAnyOrder(movementId, soundId)
                .doesNotContain(yogaId);
    }

    private void savePreferences(Set<Long> categoryIds) {
        profileService.saveMyPreferences(
                USERNAME,
                request(categoryIds)
        );

        flushAndClear();
    }

    private ClientProfilePreferencesRequest request(Set<Long> categoryIds) {
        return new ClientProfilePreferencesRequest(
                "Cliente",
                "Prueba",
                cityId,
                "preferences@example.com",
                null,
                true,
                true,
                true,
                false,
                categoryIds
        );
    }

    private List<Long> storedCategoryIds() {
        return interestRepository.findByClientProfileId(profileId)
                .stream()
                .map(relation -> relation.getCategory().getId())
                .toList();
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);

        return entityManager.persist(category);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
    @Test
    void shouldSaveExperienceTypesRepeatedlyAndReplaceOverlappingSelections() {
        Long classesId = createExperienceType("Clases").getId();
        Long workshopsId = createExperienceType("Talleres").getId();
        Long retreatsId = createExperienceType("Retiros").getId();

        Set<Long> initial = Set.of(classesId, workshopsId);

        saveExperiencePreferences(initial);
        saveExperiencePreferences(initial);

        assertThat(signalsService.getSignals(USERNAME)
                .preferredExperienceTypeIds())
                .containsExactlyInAnyOrder(classesId, workshopsId);

        saveExperiencePreferences(Set.of(workshopsId, retreatsId));

        assertThat(signalsService.getSignals(USERNAME)
                .preferredExperienceTypeIds())
                .containsExactlyInAnyOrder(workshopsId, retreatsId)
                .doesNotContain(classesId);
    }

    @Test
    void shouldValidateExperienceTypesBeforeDeletingExistingPreferences() {
        Long classesId = createExperienceType("Clases").getId();

        saveExperiencePreferences(Set.of(classesId));

        assertThrows(
                ResourceNotFoundException.class,
                () -> profileService.saveOnboardingPreferences(
                        USERNAME,
                        new OnboardingPreferencesRequest(
                                cityId,
                                Set.of(Long.MAX_VALUE),
                                EventModality.ONLINE
                        )
                )
        );

        assertThat(signalsService.getSignals(USERNAME)
                .preferredExperienceTypeIds())
                .containsExactly(classesId);
    }

    @Test
    void shouldClearExperiencePreferencesForEmptyOrNullSelection() {
        Long classesId = createExperienceType("Clases").getId();

        saveExperiencePreferences(Set.of(classesId));
        saveExperiencePreferences(Set.of());

        assertThat(signalsService.getSignals(USERNAME)
                .preferredExperienceTypeIds())
                .isEmpty();

        saveExperiencePreferences(Set.of(classesId));
        saveExperiencePreferences(null);

        assertThat(signalsService.getSignals(USERNAME)
                .preferredExperienceTypeIds())
                .isEmpty();
    }

    private ExperienceType createExperienceType(String name) {
        ExperienceType type = new ExperienceType();
        type.setName(name);

        return entityManager.persist(type);
    }

    private void saveExperiencePreferences(Set<Long> ids) {
        profileService.saveOnboardingPreferences(
                USERNAME,
                new OnboardingPreferencesRequest(
                        cityId,
                        ids,
                        EventModality.ONLINE
                )
        );

        flushAndClear();
    }
}
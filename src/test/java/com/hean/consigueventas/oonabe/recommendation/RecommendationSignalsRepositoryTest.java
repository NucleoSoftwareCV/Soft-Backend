package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.interaction.entity.EventFavorite;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import com.hean.consigueventas.oonabe.interaction.repository.EventFavoriteRepository;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:recommendation_queries;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
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
class RecommendationSignalsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventFavoriteRepository favoriteRepository;

    @Autowired
    private ProfessionalFollowRepository followRepository;

    @Test
    void shouldReturnDistinctFavoriteCategoriesForEachClient() {
        ClientProfile firstClient = createClient("first");
        ClientProfile secondClient = createClient("second");

        SpecialistProfile specialist = createSpecialist("teacher");

        Category yoga = createCategory("Yoga");
        Category meditation = createCategory("Meditacion");

        Event firstYogaEvent = createEvent("Yoga inicial", yoga, specialist);
        Event secondYogaEvent = createEvent("Yoga avanzado", yoga, specialist);
        Event meditationEvent = createEvent(
                "Meditacion guiada", meditation, specialist
        );

        saveFavorite(firstClient, firstYogaEvent);
        saveFavorite(firstClient, secondYogaEvent);
        saveFavorite(secondClient, meditationEvent);

        flushAndClear();

        assertThat(favoriteRepository
                .findFavoriteCategoryIdsByClientProfileId(firstClient.getId()))
                .containsExactly(yoga.getId());

        assertThat(favoriteRepository
                .findFavoriteCategoryIdsByClientProfileId(secondClient.getId()))
                .containsExactly(meditation.getId());
    }

    @Test
    void shouldReturnOnlyTheSpecialistsFollowedByEachClient() {
        ClientProfile firstClient = createClient("first");
        ClientProfile secondClient = createClient("second");

        SpecialistProfile firstSpecialist = createSpecialist("teacher1");
        SpecialistProfile secondSpecialist = createSpecialist("teacher2");
        SpecialistProfile thirdSpecialist = createSpecialist("teacher3");

        saveFollow(firstClient, firstSpecialist);
        saveFollow(firstClient, secondSpecialist);
        saveFollow(secondClient, thirdSpecialist);

        flushAndClear();

        assertThat(followRepository
                .findFollowedSpecialistIdsByClientProfileId(firstClient.getId()))
                .containsExactlyInAnyOrder(
                        firstSpecialist.getId(),
                        secondSpecialist.getId()
                );

        assertThat(followRepository
                .findFollowedSpecialistIdsByClientProfileId(secondClient.getId()))
                .containsExactly(thirdSpecialist.getId());
    }

    @Test
    void shouldRemoveCategorySignalOnlyAfterItsLastFavoriteIsDeleted() {
        ClientProfile client = createClient("client");
        SpecialistProfile specialist = createSpecialist("teacher");
        Category category = createCategory("Yoga");

        Event firstEvent = createEvent("Primero", category, specialist);
        Event secondEvent = createEvent("Segundo", category, specialist);

        saveFavorite(client, firstEvent);
        saveFavorite(client, secondEvent);
        flushAndClear();

        favoriteRepository.deleteByClientProfileIdAndEventId(
                client.getId(), firstEvent.getId()
        );
        flushAndClear();

        assertThat(favoriteRepository
                .findFavoriteCategoryIdsByClientProfileId(client.getId()))
                .containsExactly(category.getId());

        favoriteRepository.deleteByClientProfileIdAndEventId(
                client.getId(), secondEvent.getId()
        );
        flushAndClear();

        assertThat(favoriteRepository
                .findFavoriteCategoryIdsByClientProfileId(client.getId()))
                .isEmpty();
    }

    @Test
    void shouldRemoveSpecialistSignalAfterUnfollowing() {
        ClientProfile client = createClient("client");
        SpecialistProfile specialist = createSpecialist("teacher");

        saveFollow(client, specialist);
        flushAndClear();

        assertThat(followRepository
                .findFollowedSpecialistIdsByClientProfileId(client.getId()))
                .containsExactly(specialist.getId());

        followRepository.deleteByClientProfileIdAndSpecialistProfileId(
                client.getId(), specialist.getId()
        );
        flushAndClear();

        assertThat(followRepository
                .findFollowedSpecialistIdsByClientProfileId(client.getId()))
                .isEmpty();
    }

    @Test
    void shouldReturnEmptyListsWhenClientHasNoInteractions() {
        ClientProfile client = createClient("client");
        flushAndClear();

        assertThat(favoriteRepository
                .findFavoriteCategoryIdsByClientProfileId(client.getId()))
                .isEmpty();

        assertThat(followRepository
                .findFollowedSpecialistIdsByClientProfileId(client.getId()))
                .isEmpty();
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");

        return entityManager.persist(user);
    }

    private ClientProfile createClient(String username) {
        ClientProfile profile = new ClientProfile();
        profile.setUser(createUser(username));

        return entityManager.persist(profile);
    }

    private SpecialistProfile createSpecialist(String username) {
        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setUser(createUser(username));
        specialist.setSlug(username);
        specialist.setPublicName("Profesional " + username);
        specialist.setWhatsappPhone("+51999111222");

        return entityManager.persist(specialist);
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);

        return entityManager.persist(category);
    }

    private Event createEvent(
            String title,
            Category category,
            SpecialistProfile specialist
    ) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Evento para pruebas de recomendaciones");
        event.setModality(EventModality.ONLINE);
        event.setCategory(category);
        event.setSpecialist(specialist);

        return entityManager.persist(event);
    }

    private void saveFavorite(ClientProfile client, Event event) {
        EventFavorite favorite = new EventFavorite();
        favorite.setClientProfile(client);
        favorite.setEvent(event);

        entityManager.persist(favorite);
    }

    private void saveFollow(
            ClientProfile client,
            SpecialistProfile specialist
    ) {
        ProfessionalFollow follow = new ProfessionalFollow();
        follow.setClientProfile(client);
        follow.setSpecialistProfile(specialist);

        entityManager.persist(follow);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
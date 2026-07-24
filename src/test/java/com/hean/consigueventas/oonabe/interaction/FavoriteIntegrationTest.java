package com.hean.consigueventas.oonabe.interaction;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.common.enums.FavoriteEntityType;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.interaction.entity.Favorite;
import com.hean.consigueventas.oonabe.interaction.repository.FavoriteRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.TemporaryCustomerProfileRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FavoriteIntegrationTest {

    private static final String BASE_PATH = "/api/v1/favorites";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemporaryCustomerProfileRepository customerProfileRepository;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByUsername("user1").orElseThrow();
        
        // Asegurar que el usuario tiene un CustomerProfile asociado en los tests
        if (customerProfileRepository.findByUserId(testUser.getId()).isEmpty()) {
            CustomerProfile profile = new CustomerProfile();
            profile.setUser(testUser);
            profile.setFirstNames("User");
            profile.setLastNames("One");
            customerProfileRepository.saveAndFlush(profile);
        }

        // Obtener o crear un evento de prueba
        testEvent = eventRepository.findAll().stream().findFirst().orElseGet(() -> {
            Event e = new Event();
            e.setTitle("Test Event");
            e.setDescription("Test Description");
            return eventRepository.saveAndFlush(e);
        });
    }

    @Test
    void toggleFavoriteWorkflow() throws Exception {
        // 1. Agregar a favoritos
        String requestBody = """
                {
                    "entityType": "EVENTO",
                    "entityId": %d
                }
                """.formatted(testEvent.getId());

        mockMvc.perform(post(BASE_PATH + "/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(principal(testUser.getUsername()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(true));

        // Verificar persistencia
        CustomerProfile profile = customerProfileRepository.findByUserId(testUser.getId()).orElseThrow();
        boolean exists = favoriteRepository.existsByIdCustomerIdAndIdEntityTypeAndIdEntityId(
                profile.getId(), FavoriteEntityType.EVENTO, testEvent.getId()
        );
        assertThat(exists).isTrue();

        // 2. Obtener IDs favoritos
        mockMvc.perform(get(BASE_PATH + "/ids")
                        .with(user(principal(testUser.getUsername()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventIds[0]").value(testEvent.getId()));

        // 3. Obtener lista con detalles
        mockMvc.perform(get(BASE_PATH)
                        .with(user(principal(testUser.getUsername()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entityType").value("EVENTO"))
                .andExpect(jsonPath("$[0].entityId").value(testEvent.getId()))
                .andExpect(jsonPath("$[0].title").value(testEvent.getTitle()));

        // 4. Quitar de favoritos
        mockMvc.perform(post(BASE_PATH + "/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(principal(testUser.getUsername()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(false));

        // Verificar que ya no existe
        exists = favoriteRepository.existsByIdCustomerIdAndIdEntityTypeAndIdEntityId(
                profile.getId(), FavoriteEntityType.EVENTO, testEvent.getId()
        );
        assertThat(exists).isFalse();
    }

    @Test
    void toggleFavoriteRequiresAuthentication() throws Exception {
        String requestBody = """
                {
                    "entityType": "EVENTO",
                    "entityId": 999
                }
                """;

        mockMvc.perform(post(BASE_PATH + "/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    private UserDetailsImpl principal(String username) {
        return UserDetailsImpl.build(userRepository.findByUsername(username).orElseThrow());
    }
}
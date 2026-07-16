package com.hean.consigueventas.oonabe.community;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.community.entity.MatchRequest;
import com.hean.consigueventas.oonabe.community.repository.MatchRequestRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MatchRequestIntegrationTest {

    private static final String ENDPOINT = "/api/v1/community/match-request";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void authenticatedUserCanSubmitMatchRequest() throws Exception {
        List<Long> categoryIds = categoryIds("Yoga", "Psicologia");

        mockMvc.perform(put(ENDPOINT)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("Maria Lopez", 28, categoryIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Gracias por participar. M\u00e1s adelante te enviaremos por WhatsApp la informaci\u00f3n de tu match."))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.name").doesNotExist());

        MatchRequest saved = matchRequestRepository.findByUserId(userId("user1")).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(saved.getName()).isEqualTo("Maria Lopez");
        org.assertj.core.api.Assertions.assertThat(saved.getCategories()).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(saved.getAvailableDays()).hasSize(2);
    }

    @Test
    void secondSubmissionUpdatesExistingRequest() throws Exception {
        List<Long> firstCategories = categoryIds("Yoga");
        List<Long> updatedCategories = categoryIds("Emprendimiento", "Maternidad y Familia");
        UserDetailsImpl authenticatedUser = principal("user1");

        mockMvc.perform(put(ENDPOINT)
                        .with(user(authenticatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("Maria Inicial", 28, firstCategories)))
                .andExpect(status().isOk());

        mockMvc.perform(put(ENDPOINT)
                        .with(user(authenticatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("Maria Actualizada", 29, updatedCategories)))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(matchRequestRepository.count()).isEqualTo(1);
        MatchRequest updated = matchRequestRepository.findByUserId(authenticatedUser.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getName()).isEqualTo("Maria Actualizada");
        org.assertj.core.api.Assertions.assertThat(updated.getAge()).isEqualTo(29);
        org.assertj.core.api.Assertions.assertThat(updated.getCategories()).hasSize(2);
    }

    @Test
    void unauthenticatedUserCannotSubmit() throws Exception {
                mockMvc.perform(put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("Maria Lopez", 28, categoryIds("Yoga"))))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("No autenticado"))
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/unauthenticated"))
                .andExpect(jsonPath("$.message").value("Debes autenticarte para acceder a este recurso."))
                .andExpect(jsonPath("$.path").value(ENDPOINT));
    }

    @Test
    void ageBelowEighteenIsRejected() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("Maria Lopez", 17, categoryIds("Yoga"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/validation-error"));
    }

    @Test
    void moreThanThreeCategoriesIsRejected() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("Maria Lopez", 28,
                                categoryIds("Yoga", "Psicologia", "Movimiento", "Emprendimiento"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/validation-error"));
    }

    @Test
    void missingRequiredFieldsIsRejected() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/validation-error"));
    }

    @Test
    void unknownCategoryIsRejected() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("Maria Lopez", 28, List.of(Long.MAX_VALUE))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/not-found"));
    }

    @Test
    void inactiveCategoryIsRejected() throws Exception {
        Category category = categoryRepository.findByName("Yoga").orElseThrow();
        category.setActive(false);
        categoryRepository.saveAndFlush(category);

        mockMvc.perform(put(ENDPOINT)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload("Maria Lopez", 28, List.of(category.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/business-rule"));
    }

    private UserDetailsImpl principal(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return UserDetailsImpl.build(user);
    }

    private Long userId(String username) {
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    private List<Long> categoryIds(String... names) {
        return java.util.Arrays.stream(names)
                .map(name -> categoryRepository.findByName(name).orElseThrow().getId())
                .toList();
    }

    private String validPayload(String name, int age, List<Long> categoryIds) {
        String categories = categoryIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        return """
                {
                  "name": "%s",
                  "age": %d,
                  "email": "maria@example.com",
                  "whatsapp": "+34 600 000 000",
                  "exactZone": "Valencia, Sagunto",
                  "gender": "MUJER",
                  "languages": ["ESPANOL", "INGLES"],
                  "categoryIds": [%s],
                  "availableDays": ["LUNES", "SABADO"],
                  "expectations": "Conocer gente para practicar yoga y compartir actividades de bienestar.",
                  "descriptors": ["EMPRENDEDOR"]
                }
                """.formatted(name, age, categories);
    }
}

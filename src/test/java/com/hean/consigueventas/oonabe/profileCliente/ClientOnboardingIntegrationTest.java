package com.hean.consigueventas.oonabe.profileCliente;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientOnboardingIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CategoryRepository categoryRepository;
    @Autowired CityRepository cityRepository;
    @Autowired ExperienceTypeRepository experienceTypeRepository;

    private String token;

    @BeforeEach
    void registerAndLogin() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "onboard" + suffix;
        String email = username + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","password":"secret123",
                                 "firstName":"Nuevo","lastName":"Usuario"}
                                """.formatted(username, email)))
                .andExpect(status().isCreated());

        String login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"secret123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingRequired").value(true))
                .andReturn().getResponse().getContentAsString();
        token = objectMapper.readTree(login).get("token").asText();
    }

    @Test
    void onboardingSupportsPartialSaveAndCompletion() throws Exception {
        Long categoryId = categoryRepository.findByActiveTrueOrderByNameAsc().getFirst().getId();
        Long cityId = cityRepository.findByIsActiveTrue().getFirst().getId();
        Long typeId = experienceTypeRepository.findByActiveTrueOrderByNameAsc().getFirst().getId();

        mockMvc.perform(get("/api/v1/client-profiles/me/onboarding").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_STARTED"));

        mockMvc.perform(put("/api/v1/client-profiles/me/onboarding/interests")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryIds\":[%d]}".formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERESTS_SAVED"))
                .andExpect(jsonPath("$.categoryIds[0]").value(categoryId));

        mockMvc.perform(put("/api/v1/client-profiles/me/onboarding/preferences")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cityId":%d,"experienceTypeIds":[%d],"modality":"HIBRIDA"}
                                """.formatted(cityId, typeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.cityId").value(cityId))
                .andExpect(jsonPath("$.experienceTypeIds[0]").value(typeId))
                .andExpect(jsonPath("$.modality").value("HIBRIDA"));
    }

    @Test
    void interestsRequireAtLeastOneCategory() throws Exception {
        mockMvc.perform(put("/api/v1/client-profiles/me/onboarding/interests")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void onboardingCanBeSkippedAndRequiresAuthentication() throws Exception {
        mockMvc.perform(patch("/api/v1/client-profiles/me/onboarding/status")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SKIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SKIPPED"));

        mockMvc.perform(get("/api/v1/client-profiles/me/onboarding"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveOrMissingCatalogIdsAreRejected() throws Exception {
        Category category = new Category();
        category.setName("Inactive onboarding " + UUID.randomUUID());
        category.setActive(false);
        Long inactiveCategoryId = categoryRepository.save(category).getId();

        mockMvc.perform(put("/api/v1/client-profiles/me/onboarding/interests")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryIds\":[%d]}".formatted(inactiveCategoryId)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/client-profiles/me/onboarding/preferences")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cityId\":999999,\"experienceTypeIds\":[999999]}"))
                .andExpect(status().isNotFound());
    }

    private String bearer() {
        return "Bearer " + token;
    }
}

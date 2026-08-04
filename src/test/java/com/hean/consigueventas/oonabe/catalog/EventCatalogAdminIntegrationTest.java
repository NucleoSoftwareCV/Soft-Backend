package com.hean.consigueventas.oonabe.catalog;

import com.hean.consigueventas.oonabe.auth.repository.RefreshTokenRepository;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.RoleRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventCatalogAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ExperienceTypeRepository experienceTypeRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        User admin = new User();
        admin.setUsername("catalogadmin");
        admin.setEmail("catalogadmin@oona.es");
        admin.setPassword(passwordEncoder.encode("CatalogPass123"));
        admin.setRoles(Set.of(adminRole));
        admin.setActive(true);
        userRepository.save(admin);

        String response = mockMvc.perform(post("/api/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"catalogadmin@oona.es","password":"CatalogPass123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = response.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.findByUsername("catalogadmin").ifPresent(userRepository::delete);
        categoryRepository.findBySlug("categoria-temporal").ifPresent(categoryRepository::delete);
        experienceTypeRepository.findBySlug("experiencia-temporal").ifPresent(experienceTypeRepository::delete);
    }

    @Test
    void categoryCanBeCreatedDisabledAndDeletedWhenUnused() throws Exception {
        String response = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Categoria temporal","description":"Solo para pruebas","emoji":"✨"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emoji").value("✨"))
                .andExpect(jsonPath("$.deletable").value(true))
                .andReturn().getResponse().getContentAsString();
        Long id = extractId(response);

        mockMvc.perform(patch("/api/v1/categories/{id}/status", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/v1/categories/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void renamedCategoryKeepsItsSlugAndIsReflectedInThePublicCatalog() throws Exception {
        String response = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Categoria temporal","description":"Nombre inicial","emoji":"\u2728"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("categoria-temporal"))
                .andReturn().getResponse().getContentAsString();
        Long id = extractId(response);

        mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Categoria renombrada","description":"Nombre actualizado","emoji":"\uD83C\uDFA8"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Categoria renombrada"))
                .andExpect(jsonPath("$.slug").value("categoria-temporal"))
                .andExpect(jsonPath("$.emoji").value("\uD83C\uDFA8"));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].name").value("Categoria renombrada"));
    }

    @Test
    void experienceTypeCanBeCreatedDisabledAndDeletedWhenUnused() throws Exception {
        String response = mockMvc.perform(post("/api/v1/experience-types")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Experiencia temporal","description":"Solo para pruebas"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deletable").value(true))
                .andReturn().getResponse().getContentAsString();
        Long id = extractId(response);

        mockMvc.perform(patch("/api/v1/experience-types/{id}/status", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/v1/experience-types/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void relatedCatalogEntriesCannotBeDeleted() throws Exception {
        Long categoryId = categoryRepository.findBySlug("yoga").orElseThrow().getId();
        Long typeId = experienceTypeRepository.findBySlug("talleres").orElseThrow().getId();

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/v1/experience-types/{id}", typeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void administrativeListsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/categories/all"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/experience-types/all"))
                .andExpect(status().isUnauthorized());
    }

    private Long extractId(String response) {
        return Long.valueOf(response.replaceAll(".*\\\"id\\\":([0-9]+).*", "$1"));
    }
}

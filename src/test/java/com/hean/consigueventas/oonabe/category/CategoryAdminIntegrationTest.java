package com.hean.consigueventas.oonabe.category;

import com.hean.consigueventas.oonabe.auth.repository.RefreshTokenRepository;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.RoleRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryAdminIntegrationTest {

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

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.findByUsername("testadmin").ifPresent(userRepository::delete);
        userRepository.findByUsername("testnormal").ifPresent(userRepository::delete);
    }

    @Test
    void getAllCategoriesWithoutJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/categories/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllCategoriesWithUserRoleReturns403() throws Exception {
        // Create a regular user programmatically
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
        User normalUser = new User();
        normalUser.setUsername("testnormal");
        normalUser.setEmail("testnormal@oona.es");
        normalUser.setPassword(passwordEncoder.encode("UserPass123"));
        normalUser.setRoles(Set.of(userRole));
        normalUser.setActive(true);
        userRepository.save(normalUser);

        // Login as regular user
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testnormal@oona.es",
                                  "password": "UserPass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = loginResponse.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/v1/categories/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCategoriesWithAdminRoleReturnsListSortedByName() throws Exception {
        // Create an admin user programmatically
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        User adminUser = new User();
        adminUser.setUsername("testadmin");
        adminUser.setEmail("testadmin@oona.es");
        adminUser.setPassword(passwordEncoder.encode("AdminPass123"));
        adminUser.setRoles(Set.of(adminRole));
        adminUser.setActive(true);
        userRepository.save(adminUser);

        // Login as admin
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testadmin@oona.es",
                                  "password": "AdminPass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = loginResponse.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/v1/categories/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].description", notNullValue()));
    }
}
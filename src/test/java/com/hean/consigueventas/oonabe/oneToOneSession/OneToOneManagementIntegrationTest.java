package com.hean.consigueventas.oonabe.oneToOneSession;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OneToOneManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OneToOneServiceRepository serviceRepository;

    @Autowired
    private SpecialistProfileRepository specialistProfileRepository;

    @Test
    void professionalCanCreateService() throws Exception {
        mockMvc.perform(post("/api/v1/one-to-one-services")
                        .with(user(principal("specialist_ana")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sesion de prueba profesional",
                                  "description": "Acompanamiento individual de prueba para validar gestion.",
                                  "durationMinutes": 60,
                                  "modality": "ONLINE",
                                  "price": 45.00,
                                  "currency": ""
                                }
                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Sesion de prueba profesional"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.slug").value("sesion-de-prueba-profesional"));
    }

    @Test
    void regularUserCannotCreateService() throws Exception {
        mockMvc.perform(post("/api/v1/one-to-one-services")
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Intento usuario normal",
                                  "description": "Un usuario normal no debe crear sesiones.",
                                  "durationMinutes": 60,
                                  "modality": "ONLINE",
                                  "price": 45.00
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void professionalCannotUpdateAnotherProfessionalService() throws Exception {
        OneToOneService anaService = serviceForSpecialist("specialist_ana");

        mockMvc.perform(put("/api/v1/one-to-one-services/{id}", anaService.getId())
                        .with(user(principal("specialist_carlos")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Intento edicion ajena",
                                  "description": "Carlos no debe editar una sesion de Ana.",
                                  "durationMinutes": 60,
                                  "modality": "ONLINE",
                                  "price": 45.00
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void ownerProfessionalCanToggleStatus() throws Exception {
        OneToOneService anaService = serviceForSpecialist("specialist_ana");

        mockMvc.perform(patch("/api/v1/one-to-one-services/{id}/status", anaService.getId())
                        .with(user(principal("specialist_ana")))
                        .param("status", PublicationStatus.OCULTO.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(PublicationStatus.OCULTO.name()));
    }

    private UserDetailsImpl principal(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return UserDetailsImpl.build(user);
    }

    private OneToOneService serviceForSpecialist(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        SpecialistProfile specialist = specialistProfileRepository.findByUserId(user.getId()).orElseThrow();
        return serviceRepository.findBySpecialistId(specialist.getId()).stream()
                .findFirst()
                .orElseThrow();
    }
}

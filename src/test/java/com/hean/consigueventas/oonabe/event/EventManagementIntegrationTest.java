package com.hean.consigueventas.oonabe.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.experienceType.repository.ExperienceTypeRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExperienceTypeRepository experienceTypeRepository;

    @Autowired
    private SpecialistProfileRepository specialistProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void professionalCreatesOnlineEventWithItsFirstOccurrence() throws Exception {
        UserDetailsImpl professional = principal("specialist_ana");
        Long specialistId = specialistProfileRepository.findByUserId(professional.getId())
                .orElseThrow()
                .getId();
        Long categoryId = categoryRepository.findByActiveTrueOrderByNameAsc().getFirst().getId();
        Long experienceTypeId = experienceTypeRepository.findByActiveTrueOrderByNameAsc().getFirst().getId();

        MvcResult creationResult = mockMvc.perform(post("/api/v1/events")
                        .with(user(professional))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "event": {
                                    "title": "Evento online de prueba",
                                    "summary": "Prueba de creacion online",
                                    "description": "Comprueba que el enlace se persiste despues de la ocurrencia.",
                                    "includes": [],
                                    "highlights": [],
                                    "whatToBring": [],
                                    "modality": "ONLINE",
                                    "priceFrom": 0,
                                    "currency": "EUR",
                                    "minimumAge": 18,
                                    "featured": false,
                                    "categoryId": %d,
                                    "experienceTypeId": %d,
                                    "specialistId": %d
                                  },
                                  "occurrence": {
                                    "startsAt": "2030-08-05T15:00:00Z",
                                    "endsAt": "2030-08-05T17:00:00Z",
                                    "capacity": 12,
                                    "location": null,
                                    "meetingLink": {
                                      "platform": "MEET",
                                      "meetingUrl": "https://meet.google.com/abc-defg-hij"
                                    }
                                  }
                                }
                                """.formatted(categoryId, experienceTypeId, specialistId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event.id").isNumber())
                .andExpect(jsonPath("$.occurrence.meetingLink.meetingUrl")
                        .value("https://meet.google.com/abc-defg-hij"))
                .andReturn();

        JsonNode creationResponse = objectMapper.readTree(creationResult.getResponse().getContentAsString());
        long eventId = creationResponse.path("event").path("id").asLong();

        mockMvc.perform(get("/api/v1/events/{id}/management", eventId)
                        .with(user(professional)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.description")
                        .value("Comprueba que el enlace se persiste despues de la ocurrencia."))
                .andExpect(jsonPath("$.occurrences[0].startsAt").value("2030-08-05T15:00:00Z"))
                .andExpect(jsonPath("$.occurrences[0].endsAt").value("2030-08-05T17:00:00Z"))
                .andExpect(jsonPath("$.occurrences[0].capacity").value(12))
                .andExpect(jsonPath("$.occurrences[0].meetingLink.meetingUrl")
                        .value("https://meet.google.com/abc-defg-hij"));
    }

    @Test
    void professionalListsOnlyOwnEvents() throws Exception {
        mockMvc.perform(get("/api/v1/events/my-events")
                        .with(user(principal("specialist_ana"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].specialistId").exists());
    }

    @Test
    void professionalCannotManageAnotherOwnersEvent() throws Exception {
        Event event = eventRepository.findBySpecialistUserId(
                        principal("specialist_ana").getId(),
                        org.springframework.data.domain.Pageable.ofSize(1))
                .getContent()
                .getFirst();

        mockMvc.perform(get("/api/v1/events/{id}/management", event.getId())
                        .with(user(principal("specialist_carlos"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanChangeEventStatus() throws Exception {
        UserDetailsImpl ana = principal("specialist_ana");
        Event event = eventRepository.findBySpecialistUserId(
                        ana.getId(),
                        org.springframework.data.domain.Pageable.ofSize(1))
                .getContent()
                .getFirst();

        mockMvc.perform(patch("/api/v1/events/{id}/status", event.getId())
                        .with(user(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ARCHIVADO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVADO"));
    }

    @Test
    void managementListingRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/events/my-events"))
                .andExpect(status().isUnauthorized());
    }

    private UserDetailsImpl principal(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return UserDetailsImpl.build(user);
    }
}

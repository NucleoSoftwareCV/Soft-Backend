package com.hean.consigueventas.oonabe.oneToOneSession;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.masterdata.repository.TechniqueRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.WorkTopicRepository;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OneToOnePublicListingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OneToOneServiceRepository serviceRepository;

    @Autowired
    private WorkTopicRepository workTopicRepository;

    @Autowired
    private TechniqueRepository techniqueRepository;

    private Long serviceId;
    private String serviceSlug;
    private String serviceSearchTerm;
    private Long workTopicId;
    private Long techniqueId;
    private Long specialistId;

    @BeforeEach
    void setUpFilterData() {
        OneToOneService service = serviceRepository.findAll().stream()
                .filter(item -> item.getStatus() == PublicationStatus.PUBLICADO)
                .findFirst()
                .orElseThrow();
        WorkTopic workTopic = workTopicRepository.findByNameIgnoreCase("Bienestar").orElseThrow();
        Technique technique = techniqueRepository.findByNameIgnoreCase("Terapia").orElseThrow();

        service.setWorkTopics(new HashSet<>(service.getWorkTopics()));
        service.setTechniques(new HashSet<>(service.getTechniques()));
        service.getWorkTopics().add(workTopic);
        service.getTechniques().add(technique);
        OneToOneService saved = serviceRepository.save(service);

        serviceId = saved.getId();
        serviceSlug = saved.getSlug();
        serviceSearchTerm = saved.getTitle().substring(0, Math.min(8, saved.getTitle().length()));
        workTopicId = workTopic.getId();
        techniqueId = technique.getId();
        specialistId = saved.getSpecialist().getId();
    }

    @Test
    void publicListingIsPaginatedAndOnlyExposesCardFields() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(12)))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].specialistName").exists())
                .andExpect(jsonPath("$.content[0].specialistPhotoUrl").exists())
                .andExpect(jsonPath("$.content[0].price").exists())
                .andExpect(jsonPath("$.content[0].currency").exists())
                .andExpect(jsonPath("$.content[0].durationMinutes").exists())
                .andExpect(jsonPath("$.content[0].imageUrl").exists())
                .andExpect(jsonPath("$.content[0]", not(hasKey("slug"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("description"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("status"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("modality"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("locationName"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("createdAt"))))
                .andExpect(jsonPath("$.content[0]", not(hasKey("updatedAt"))));
    }

    @Test
    void publicListingFiltersByWorkTopic() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("workTopicId", workTopicId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].id").value(hasItem(serviceId.intValue())));
    }

    @Test
    void publicListingFiltersByTechnique() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("techniqueId", techniqueId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].id").value(hasItem(serviceId.intValue())));
    }

    @Test
    void publicListingFiltersByWorkTopicAndTechnique() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("workTopicId", workTopicId.toString())
                        .param("techniqueId", techniqueId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].id").value(hasItem(serviceId.intValue())));
    }

    @Test
    void publicListingFiltersBySpecialistId() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("specialistId", specialistId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].id").value(hasItem(serviceId.intValue())));
    }

    @Test
    void publicListingSearchesByText() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("search", serviceSearchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].id").value(hasItem(serviceId.intValue())));
    }

    @Test
    void publicDetailByIdWorksWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.specialistPhotoUrl").exists())
                .andExpect(jsonPath("$.specialistWhatsappPhone").exists());
    }

    @Test
    void publicDetailBySlugWorksWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services/slug/{slug}", serviceSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId))
                .andExpect(jsonPath("$.slug").value(serviceSlug));
    }

    @Test
    void secondPageUsesRequestedSizeAndOnlyPublishedSessions() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("page", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(12)))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    void swaggerExampleSortIsIgnoredInsteadOfFailing() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "[\"string\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(12)));
    }

    @Test
    void publicListingCapsPageSizeToOneHundred() throws Exception {
        mockMvc.perform(get("/api/v1/one-to-one-services")
                        .param("page", "0")
                        .param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(12)));
    }
}

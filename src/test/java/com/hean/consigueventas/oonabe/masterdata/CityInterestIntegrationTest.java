package com.hean.consigueventas.oonabe.masterdata;

import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.repository.CityInterestRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CityInterestIntegrationTest {

    private static final String ENDPOINT = "/api/v1/city-interests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CityInterestRepository cityInterestRepository;

    @Test
    void anyoneCanRegisterInterestForAnActiveCity() throws Exception {
        City valencia = activeCity("Valencia");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(valencia.getId(), "usuario@email.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(
                        "Gracias por tu interes. Te avisaremos cuando Oona llegue a tu ciudad."))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());

        assertThat(cityInterestRepository.existsByCityIdAndEmailIgnoreCase(valencia.getId(), "usuario@email.com"))
                .isTrue();
    }

    @Test
    void emailIsNormalizedBeforeStoring() throws Exception {
        City valencia = activeCity("Valencia");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(valencia.getId(), "Usuario@Email.COM")))
                .andExpect(status().isCreated());

        assertThat(cityInterestRepository.existsByCityIdAndEmailIgnoreCase(valencia.getId(), "usuario@email.com"))
                .isTrue();
    }

    @Test
    void invalidEmailIsRejected() throws Exception {
        City valencia = activeCity("Valencia");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(valencia.getId(), "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/validation-error"));
    }

    @Test
    void nonExistentCityIsRejected() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(Long.MAX_VALUE, "usuario@email.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/not-found"));
    }

    @Test
    void inactiveCityIsRejected() throws Exception {
        City valencia = activeCity("Valencia");
        valencia.setIsActive(false);
        cityRepository.saveAndFlush(valencia);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(valencia.getId(), "usuario@email.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/business-rule"));
    }

    @Test
    void duplicateInterestForSameCityAndEmailIsRejected() throws Exception {
        City valencia = activeCity("Valencia");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(valencia.getId(), "usuario@email.com")))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(valencia.getId(), "usuario@email.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/business-rule"));

        assertThat(cityInterestRepository.count()).isEqualTo(1);
    }

    private City activeCity(String name) {
        return cityRepository.findAll().stream()
                .filter(city -> city.getName().equals(name) && Boolean.TRUE.equals(city.getIsActive()))
                .findFirst()
                .orElseThrow();
    }

    private String payload(Long cityId, String email) {
        return """
                {
                  "cityId": %d,
                  "email": "%s"
                }
                """.formatted(cityId, email);
    }
}

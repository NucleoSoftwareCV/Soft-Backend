package com.hean.consigueventas.oonabe.booking;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.service.ProfessionalDirectoryService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Run only against a disposable PostgreSQL database; never a development or production database. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@EnabledIfEnvironmentVariable(named="OONA_DIRECTORY_TEST_URL", matches="jdbc:postgresql:.*")
class ProfessionalDirectoryIntegrationTest {
    @DynamicPropertySource static void postgres(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv("OONA_DIRECTORY_TEST_URL"));
        r.add("spring.datasource.username", () -> "oona_directory_check");
        r.add("spring.datasource.password", () -> "");
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
    @Autowired EntityManager em;
    @Autowired MockMvc mvc;
    @Autowired ProfessionalDirectoryService directory;
    BookingFixtures.Fixture fixture;

    @BeforeEach void data() {
        fixture=BookingFixtures.create(em,"dircheck");
        var specialist=fixture.occurrence().getEvent().getSpecialist();
        specialist.setPublicName("Profesional DirCheck");
        City city=new City(); city.setName("Lima DirCheck"); city.setProvince("Lima"); city.setCountryCode("PE"); em.persist(city);
        Location location=new Location(); location.setName("Sede DirCheck"); location.setAddress("Dirección de prueba"); location.setIsActive(true); location.setCity(city); em.persist(location);
        fixture.occurrence().setLocation(location);
        fixture.occurrence().getEvent().setModality(EventModality.PRESENCIAL);
        fixture.occurrence().getEvent().setCurrency("PEN");
        em.flush();
    }
    @Test void publicDirectoryReturnsRealProfilePeruCityAndPrice() throws Exception {
        mvc.perform(get("/api/v1/specialist-profiles/directory").param("search","DirCheck"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].publicName").value("Profesional DirCheck"))
            .andExpect(jsonPath("$.content[0].currency").value("PEN"))
            .andExpect(jsonPath("$.content[0].priceFrom").value(25))
            .andExpect(jsonPath("$.content[0].cityNames[0]").value("Lima DirCheck"))
            .andExpect(jsonPath("$.content[0].reviewCount").value(0));
    }
    @Test void combinesLocationFiltersAndDoesNotExposeDraftProfiles() {
        assertThat(directory.get("DirCheck","Lima DirCheck","professionals","",0,20).totalElements()).isEqualTo(1);
        assertThat(directory.get("DirCheck","Cusco","professionals","",0,20).totalElements()).isZero();
        fixture.occurrence().getEvent().getSpecialist().setPublicationStatus(PublicationStatus.BORRADOR);
        em.flush();
        assertThat(directory.get("DirCheck","","professionals","",0,20).totalElements()).isZero();
    }
    @Test void rejectsInvalidPaginationBeforeQuerying() throws Exception {
        mvc.perform(get("/api/v1/specialist-profiles/directory").param("page","-1"))
            .andExpect(status().isBadRequest());
    }
}

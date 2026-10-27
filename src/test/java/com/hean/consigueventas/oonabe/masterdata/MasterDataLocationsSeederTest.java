package com.hean.consigueventas.oonabe.masterdata;

import com.hean.consigueventas.oonabe.masterdata.config.MasterDataSeeder;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:map_location_seeds;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MasterDataSeeder.class)
class MasterDataLocationsSeederTest {
    @Autowired MasterDataSeeder seeder;
    @Autowired LocationRepository locations;
    @Autowired TestEntityManager em;

    @Test
    void createsThreeApproximateLocationsAndDoesNotDuplicateOnRestart() {
        seeder.seed();
        em.flush();
        em.clear();
        seeder.seed();
        em.flush();
        em.clear();

        assertThat(locations.count()).isEqualTo(3);
        assertPoint("Centro Holistico Miraflores", "Valencia", "39.474000", "-0.380000");
        assertPoint("Casa Bienestar San Isidro", "Barcelona", "41.389000", "2.159000");
        assertPoint("Cada de vista", "Madrid", "40.417000", "-3.703000");
    }

    @Test
    void backfillsOriginalSeedWithoutChangingItsId() {
        Location location = originalValenciaSeed();
        Long id = location.getId();
        seeder.seed();
        em.flush();
        em.clear();

        assertThat(locations.findById(id)).isPresent();
        assertPoint("Centro Holistico Miraflores", "Valencia", "39.474000", "-0.380000");
        assertThat(locations.count()).isEqualTo(3);
    }

    @Test
    void preservesRealCoordinatesAddressReferenceAndInactiveStatus() {
        Location location = originalValenciaSeed();
        location.setAddress("Calle de la sede confirmada 12");
        location.setReference("Entrada por el patio");
        location.setLatitude(new BigDecimal("39.490000"));
        location.setLongitude(new BigDecimal("-0.400000"));
        location.setIsActive(false);
        seeder.seed();
        em.flush();
        em.clear();

        Location actual = locations.findById(location.getId()).orElseThrow();
        assertThat(actual.getAddress()).isEqualTo("Calle de la sede confirmada 12");
        assertThat(actual.getReference()).isEqualTo("Entrada por el patio");
        assertThat(actual.getLatitude()).isEqualByComparingTo("39.490000");
        assertThat(actual.getLongitude()).isEqualByComparingTo("-0.400000");
        assertThat(actual.getIsActive()).isFalse();
    }

    @Test
    void preservesManuallyEditedAddressEvenWithoutCoordinates() {
        Location location = originalValenciaSeed();
        location.setAddress("Dirección pendiente de geolocalizar");
        seeder.seed();
        em.flush();
        em.clear();

        Location actual = locations.findById(location.getId()).orElseThrow();
        assertThat(actual.getAddress()).isEqualTo("Dirección pendiente de geolocalizar");
        assertThat(actual.getLatitude()).isNull();
        assertThat(actual.getLongitude()).isNull();
    }

    @Test
    void doesNotCombineAnExistingLatitudeWithAnApproximateLongitude() {
        Location location = originalValenciaSeed();
        location.setLatitude(new BigDecimal("39.500000"));
        seeder.seed();
        em.flush();
        em.clear();

        Location actual = locations.findById(location.getId()).orElseThrow();
        assertThat(actual.getLatitude()).isEqualByComparingTo("39.500000");
        assertThat(actual.getLongitude()).isNull();
        assertThat(actual.getAddress()).isEqualTo("Av. Larco 123");
    }

    @Test
    void preservesAnExistingLocationMovedToAnotherCity() {
        Location location = originalValenciaSeed();
        location.setCity(locations.findByName("Casa Bienestar San Isidro").orElseThrow().getCity());
        seeder.seed();
        em.flush();
        em.clear();

        Location actual = locations.findById(location.getId()).orElseThrow();
        assertThat(actual.getCity().getName()).isEqualTo("Barcelona");
        assertThat(actual.getLatitude()).isNull();
        assertThat(actual.getLongitude()).isNull();
    }

    private Location originalValenciaSeed() {
        Location location = seeder.seed().loc1();
        location.setAddress("Av. Larco 123");
        location.setReference("LINK");
        location.setLatitude(null);
        location.setLongitude(null);
        em.flush();
        return location;
    }

    private void assertPoint(String name, String city, String latitude, String longitude) {
        Location location = locations.findByName(name).orElseThrow();
        assertThat(location.getCity().getName()).isEqualTo(city);
        assertThat(location.getAddress()).isEqualTo(
                "Ubicación aproximada en " + city + ". Dirección exacta pendiente de confirmar.");
        assertThat(location.getLatitude()).isEqualByComparingTo(latitude);
        assertThat(location.getLongitude()).isEqualByComparingTo(longitude);
        assertThat(location.getReference()).contains("Punto orientativo");
    }
}
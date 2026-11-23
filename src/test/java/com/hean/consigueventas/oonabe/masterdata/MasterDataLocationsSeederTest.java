package com.hean.consigueventas.oonabe.masterdata;

import com.hean.consigueventas.oonabe.masterdata.config.MasterDataSeeder;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
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
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MasterDataSeeder.class)
class MasterDataLocationsSeederTest {
    @Autowired MasterDataSeeder seeder;
    @Autowired LocationRepository locations;
    @Autowired CityRepository cities;
    @Autowired TestEntityManager em;

    @Test void createsPeruCatalogueAndDistinctApproximatePointsWithoutDuplicates() {
        seeder.seed(); reload(); seeder.seed(); reload();
        assertThat(cities.findByIsActiveTrue()).hasSize(36)
                .allSatisfy(city -> assertThat(city.getCountryCode()).isEqualTo("PE"));
        assertThat(locations.count()).isEqualTo(3);
        assertPoint("Centro Holistico Miraflores", "Miraflores", "-12.121000", "-77.029000");
        assertPoint("Casa Bienestar San Isidro", "San Isidro", "-12.099000", "-77.035000");
        assertPoint("Cada de vista", "Lima Cercado", "-12.046000", "-77.043000");
    }

    @Test void migratesOriginalSeedAndKeepsItsIdAndInactiveStatus() {
        Location location = rawSeed(); Long id = location.getId(); location.setIsActive(false);
        seeder.seed(); reload();
        assertPoint("Centro Holistico Miraflores", "Miraflores", "-12.121000", "-77.029000");
        assertThat(locations.findById(id).orElseThrow().getIsActive()).isFalse();
        assertThat(locations.count()).isEqualTo(3);
    }

    @Test void migratesAllExactLegacyApproximatePointsAndHidesOldCities() {
        seeder.seed();
        oldApproximate("Centro Holistico Miraflores", "Valencia", "39.474000", "-0.380000");
        oldApproximate("Casa Bienestar San Isidro", "Barcelona", "41.389000", "2.159000");
        oldApproximate("Cada de vista", "Madrid", "40.417000", "-3.703000");
        reload(); seeder.seed(); reload();
        assertPoint("Centro Holistico Miraflores", "Miraflores", "-12.121000", "-77.029000");
        assertPoint("Casa Bienestar San Isidro", "San Isidro", "-12.099000", "-77.035000");
        assertPoint("Cada de vista", "Lima Cercado", "-12.046000", "-77.043000");
        assertThat(cities.findByIsActiveTrue()).hasSize(36).allMatch(c -> "PE".equals(c.getCountryCode()));
        assertThat(cities.findByNameAndProvince("Valencia", "Valencia")).isPresent();
        assertThat(cities.findByNameAndProvince("Valencia", "Valencia").orElseThrow().getIsActive()).isFalse();
    }

    @Test void preservesConfirmedAddressCoordinatesReferenceAndInactiveStatus() {
        Location location = rawSeed();
        location.setAddress("Dirección confirmada de la sede 12"); location.setReference("Entrada por el patio");
        location.setLatitude(new BigDecimal("-12.115000")); location.setLongitude(new BigDecimal("-77.031000"));
        location.setIsActive(false); Long id=location.getId();
        seeder.seed(); reload();
        Location actual = locations.findById(id).orElseThrow();
        assertThat(actual.getAddress()).isEqualTo("Dirección confirmada de la sede 12");
        assertThat(actual.getReference()).isEqualTo("Entrada por el patio");
        assertThat(actual.getLatitude()).isEqualByComparingTo("-12.115000");
        assertThat(actual.getLongitude()).isEqualByComparingTo("-77.031000");
        assertThat(actual.getIsActive()).isFalse();
    }

    @Test void preservesManuallyEditedAddressEvenWithoutCoordinates() {
        Location location = rawSeed(); location.setAddress("Dirección pendiente de geolocalizar"); Long id=location.getId();
        seeder.seed(); reload();
        Location actual=locations.findById(id).orElseThrow();
        assertThat(actual.getAddress()).isEqualTo("Dirección pendiente de geolocalizar");
        assertThat(actual.getLatitude()).isNull(); assertThat(actual.getLongitude()).isNull();
    }

    @Test void neverCombinesAnExistingLatitudeWithAnApproximateLongitude() {
        Location location=rawSeed(); location.setLatitude(new BigDecimal("-12.115")); Long id=location.getId();
        seeder.seed(); reload();
        Location actual=locations.findById(id).orElseThrow();
        assertThat(actual.getLatitude()).isEqualByComparingTo("-12.115");
        assertThat(actual.getLongitude()).isNull(); assertThat(actual.getAddress()).isEqualTo("Av. Larco 123");
    }

    @Test void preservesAnExistingLocationMovedToAnotherCity() {
        Location location=rawSeed(); location.setCity(cities.findByNameAndProvince("Cusco", "Cusco").orElseThrow());
        Long id=location.getId(); seeder.seed(); reload();
        Location actual=locations.findById(id).orElseThrow();
        assertThat(actual.getCity().getName()).isEqualTo("Cusco");
        assertThat(actual.getLatitude()).isNull(); assertThat(actual.getLongitude()).isNull();
    }

    @Test void preservesCoordinatesCorrectedAfterPeruMigrationOnRestart() {
        Location location=seeder.seed().loc1(); location.setLatitude(new BigDecimal("-12.125"));
        Long id=location.getId(); seeder.seed(); reload();
        assertThat(locations.findById(id).orElseThrow().getLatitude()).isEqualByComparingTo("-12.125");
    }

    @Test void preservesManuallyCorrectedLegacyPointInsteadOfMovingIt() {
        seeder.seed(); oldApproximate("Centro Holistico Miraflores", "Valencia", "39.474000", "-0.380000");
        Location location=locations.findByName("Centro Holistico Miraflores").orElseThrow();
        location.setLongitude(new BigDecimal("-0.381")); Long id=location.getId();
        seeder.seed(); reload();
        assertThat(locations.findById(id).orElseThrow().getLongitude()).isEqualByComparingTo("-0.381");
    }

    private Location rawSeed() {
        Location location=seeder.seed().loc1();
        location.setAddress("Av. Larco 123"); location.setReference("LINK");
        location.setLatitude(null); location.setLongitude(null); em.flush(); return location;
    }
    private void oldApproximate(String name, String cityName, String lat, String lon) {
        City city=new City(); city.setName(cityName); city.setProvince(cityName); city.setCountryCode("ES");
        city.setIsActive(true); cities.save(city);
        Location location=locations.findByName(name).orElseThrow(); location.setCity(city);
        location.setAddress("Ubicación aproximada en " + cityName + ". Dirección exacta pendiente de confirmar.");
        location.setReference("Punto orientativo de ciudad; confirmar la dirección con el organizador.");
        location.setLatitude(new BigDecimal(lat)); location.setLongitude(new BigDecimal(lon));
    }
    private void reload() { em.flush(); em.clear(); }
    private void assertPoint(String name, String district, String latitude, String longitude) {
        Location location=locations.findByName(name).orElseThrow();
        assertThat(location.getCity().getName()).isEqualTo("Lima");
        assertThat(location.getAddress()).isEqualTo("Ubicación aproximada en " + district
                + ", Lima. Dirección exacta pendiente de confirmar.");
        assertThat(location.getLatitude()).isEqualByComparingTo(latitude);
        assertThat(location.getLongitude()).isEqualByComparingTo(longitude);
        assertThat(location.getReference()).contains("Punto orientativo");
    }
}

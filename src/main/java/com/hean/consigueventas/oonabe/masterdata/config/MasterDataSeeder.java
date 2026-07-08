package com.hean.consigueventas.oonabe.masterdata.config;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.TechniqueRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.WorkTopicRepository;
import org.springframework.stereotype.Component;

@Component
public class MasterDataSeeder {

    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final LocationRepository locationRepository;
    private final WorkTopicRepository workTopicRepository;
    private final TechniqueRepository techniqueRepository;

    public MasterDataSeeder(
            CategoryRepository categoryRepository,
            CityRepository cityRepository,
            LocationRepository locationRepository,
            WorkTopicRepository workTopicRepository,
            TechniqueRepository techniqueRepository) {
        this.categoryRepository = categoryRepository;
        this.cityRepository = cityRepository;
        this.locationRepository = locationRepository;
        this.workTopicRepository = workTopicRepository;
        this.techniqueRepository = techniqueRepository;
    }

    public SeedData seed() {
        seedCategory("Yoga", "Practicas de yoga y bienestar corporal.");
        seedCategory("Hielo y Breathwork", "Experiencias de respiracion consciente y exposicion al frio.");
        seedCategory("Arte y Creatividad", "Actividades creativas para expresion y bienestar.");
        seedCategory("Movimiento", "Experiencias de movimiento consciente.");
        seedCategory("Deporte", "Actividades fisicas orientadas al bienestar.");
        seedCategory("Meditacion y Mindfulness", "Practicas de atencion plena y meditacion.");
        seedCategory("Sonido y Vibracion", "Experiencias de sonido, vibracion y relajacion.");
        seedCategory("Espiritualidad y Energia", "Practicas energeticas y espirituales.");
        seedCategory("Nutricion y Cocina", "Experiencias de alimentacion consciente.");
        seedCategory("Psicologia", "Acompanamiento psicologico y bienestar emocional.");
        seedCategory("Cuerpo y Salud", "Practicas centradas en salud corporal integral.");

        seedCity("Madrid", "Madrid");
        seedCity("Barcelona", "Barcelona");
        seedCity("Valencia", "Valencia");

        Location loc1 = seedLocation("Centro Holistico Miraflores",
                "Av. Larco 123", "LINK", "Valencia", "Valencia", true);
        Location loc2 = seedLocation("Casa Bienestar San Isidro",
                "Av. Javier Prado 456", "LINK", "Barcelona", "Barcelona", true);
        Location loc3 = seedLocation("Cada de vista", "hola", "Acceso",
                "Madrid", "Madrid", true);

        seedWorkTopic("Autoestima", true);
        seedWorkTopic("Motivacion", true);
        seedWorkTopic("Bienestar", true);
        seedWorkTopic("Resiliencia", true);

        seedTechnique("Terapia", true);
        seedTechnique("Acupuntura", true);
        seedTechnique("Quiropraxia", true);
        seedTechnique("Yoga", true);

        return new SeedData(
                categoryRepository.findByName("Cuerpo y Salud").orElse(null),
                categoryRepository.findByName("Movimiento").orElse(null),
                categoryRepository.findByName("Sonido y Vibracion").orElse(null),
                categoryRepository.findByName("Hielo y Breathwork").orElse(null),
                categoryRepository.findByName("Yoga").orElse(null),
                categoryRepository.findByName("Meditacion y Mindfulness").orElse(null),
                categoryRepository.findByName("Nutricion y Cocina").orElse(null),
                loc1,
                loc2,
                loc3
        );
    }

    private void seedCategory(String name, String description) {
        categoryRepository.findByName(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            category.setActive(true);
            return categoryRepository.save(category);
        });
    }

    private Location seedLocation(
            String name,
            String address,
            String reference,
            String cityName,
            String provinceName,
            boolean isActive) {

        return locationRepository.findByName(name).orElseGet(() -> {
            Location location = new Location();
            location.setName(name);
            location.setAddress(address);
            location.setReference(reference);
            location.setIsActive(isActive);
            if (cityName != null && provinceName != null) {
                location.setCity(cityRepository.findByNameAndProvince(cityName, provinceName).orElse(null));
            }
            return locationRepository.save(location);
        });
    }

    private void seedCity(String name, String province) {
        cityRepository.findByNameAndProvince(name, province)
                .orElseGet(() -> {
                    City city = new City();
                    city.setName(name.trim());
                    city.setProvince(province.trim());
                    city.setCountryCode("ES");
                    city.setIsActive(true);
                    return cityRepository.save(city);
                });
    }

    private void seedWorkTopic(String name, boolean active) {
        String normalizedName = name.trim();
        workTopicRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> {
                    WorkTopic workTopic = new WorkTopic();
                    workTopic.setName(normalizedName);
                    workTopic.setActive(active);
                    return workTopicRepository.save(workTopic);
                });
    }

    private void seedTechnique(String name, boolean active) {
        String normalizedName = name.trim();
        techniqueRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> {
                    Technique technique = new Technique();
                    technique.setName(normalizedName);
                    technique.setActive(active);
                    return techniqueRepository.save(technique);
                });
    }

    public record SeedData(
            Category catCuerpo,
            Category catMovimiento,
            Category catSonido,
            Category catHielo,
            Category catYoga,
            Category catMeditacion,
            Category catNutricion,
            Location loc1,
            Location loc2,
            Location loc3
    ) {
    }
}

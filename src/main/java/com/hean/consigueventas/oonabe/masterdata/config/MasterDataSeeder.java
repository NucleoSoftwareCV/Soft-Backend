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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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
        seedCategory("Maternidad y Familia", "Experiencias de bienestar para la maternidad y la familia.");
        seedCategory("Emprendimiento", "Experiencias para emprender con bienestar y proposito.");

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
        Category category = categoryRepository.findByName(name).orElse(null);
        if (category == null) {
            Category newCategory = new Category();
            newCategory.setName(name);
            newCategory.setDescription(description);
            newCategory.setActive(true);
            categoryRepository.save(newCategory);
        } else {
            boolean changed = false;
            if (!description.equals(category.getDescription())) {
                category.setDescription(description);
                changed = true;
            }
            if (!category.isActive()) {
                category.setActive(true);
                changed = true;
            }
            if (changed) {
                categoryRepository.save(category);
            }
        }
    }

    private Location seedLocation(
            String name,
            String address,
            String reference,
            String cityName,
            String provinceName,
            boolean isActive) {

        Location location = locationRepository.findByName(name).orElse(null);
        City targetCity = (cityName != null && provinceName != null)
                ? cityRepository.findByNameAndProvince(cityName, provinceName).orElse(null)
                : null;

        if (location == null) {
            Location newLocation = new Location();
            newLocation.setName(name);
            newLocation.setAddress(address);
            newLocation.setReference(reference);
            newLocation.setIsActive(isActive);
            newLocation.setCity(targetCity);
            return locationRepository.save(newLocation);
        } else {
            boolean changed = false;
            if (!address.equals(location.getAddress())) {
                location.setAddress(address);
                changed = true;
            }
            if (!reference.equals(location.getReference())) {
                location.setReference(reference);
                changed = true;
            }
            if (location.getIsActive() != isActive) {
                location.setIsActive(isActive);
                changed = true;
            }
            if (targetCity != null && !targetCity.equals(location.getCity())) {
                location.setCity(targetCity);
                changed = true;
            }
            if (changed) {
                return locationRepository.save(location);
            }
            return location;
        }
    }

    private void seedCity(String name, String province) {
        City city = cityRepository.findByNameAndProvince(name, province).orElse(null);
        if (city == null) {
            City newCity = new City();
            newCity.setName(name.trim());
            newCity.setProvince(province.trim());
            newCity.setCountryCode("ES");
            newCity.setIsActive(true);
            cityRepository.save(newCity);
        } else {
            boolean changed = false;
            if (!"ES".equals(city.getCountryCode())) {
                city.setCountryCode("ES");
                changed = true;
            }
            if (!city.getIsActive()) {
                city.setIsActive(true);
                changed = true;
            }
            if (changed) {
                cityRepository.save(city);
            }
        }
    }

    private void seedWorkTopic(String name, boolean active) {
        String normalizedName = name.trim();
        WorkTopic workTopic = workTopicRepository.findByNameIgnoreCase(normalizedName).orElse(null);
        if (workTopic == null) {
            WorkTopic newWorkTopic = new WorkTopic();
            newWorkTopic.setName(normalizedName);
            newWorkTopic.setActive(active);
            workTopicRepository.save(newWorkTopic);
        } else {
            boolean changed = false;
            if (workTopic.isActive() != active) {
                workTopic.setActive(active);
                changed = true;
            }
            if (changed) {
                workTopicRepository.save(workTopic);
            }
        }
    }

    private void seedTechnique(String name, boolean active) {
        String normalizedName = name.trim();
        Technique technique = techniqueRepository.findByNameIgnoreCase(normalizedName).orElse(null);
        if (technique == null) {
            Technique newTechnique = new Technique();
            newTechnique.setName(normalizedName);
            newTechnique.setActive(active);
            techniqueRepository.save(newTechnique);
        } else {
            boolean changed = false;
            if (technique.isActive() != active) {
                technique.setActive(active);
                changed = true;
            }
            if (changed) {
                techniqueRepository.save(technique);
            }
        }
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

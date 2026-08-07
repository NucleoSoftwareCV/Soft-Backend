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
        boolean createDefaultCategories = categoryRepository.count() == 0;
        seedCategory("Yoga", "Practicas de yoga y bienestar corporal.", "🧘", createDefaultCategories);
        seedCategory("Hielo y Breathwork", "Experiencias de respiracion consciente y exposicion al frio.", "🧊", createDefaultCategories);
        seedCategory("Arte y Creatividad", "Actividades creativas para expresion y bienestar.", "🎨", createDefaultCategories);
        seedCategory("Movimiento", "Experiencias de movimiento consciente.", "🏃", createDefaultCategories);
        seedCategory("Deporte", "Actividades fisicas orientadas al bienestar.", "💪", createDefaultCategories);
        seedCategory("Meditacion y Mindfulness", "Practicas de atencion plena y meditacion.", "🧠", createDefaultCategories);
        seedCategory("Sonido y Vibracion", "Experiencias de sonido, vibracion y relajacion.", "🎵", createDefaultCategories);
        seedCategory("Espiritualidad y Energia", "Practicas energeticas y espirituales.", "✨", createDefaultCategories);
        seedCategory("Nutricion y Cocina", "Experiencias de alimentacion consciente.", "🥗", createDefaultCategories);
        seedCategory("Psicologia", "Acompanamiento psicologico y bienestar emocional.", "🌱", createDefaultCategories);
        seedCategory("Cuerpo y Salud", "Practicas centradas en salud corporal integral.", "💆", createDefaultCategories);
        seedCategory("Maternidad y Familia", "Experiencias de bienestar para la maternidad y la familia.", "🤰", createDefaultCategories);
        seedCategory("Emprendimiento", "Experiencias para emprender con bienestar y proposito.", "🚀", createDefaultCategories);

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
                categoryRepository.findBySlug("cuerpo-y-salud").orElse(null),
                categoryRepository.findBySlug("movimiento").orElse(null),
                categoryRepository.findBySlug("sonido-y-vibracion").orElse(null),
                categoryRepository.findBySlug("hielo-y-breathwork").orElse(null),
                categoryRepository.findBySlug("yoga").orElse(null),
                categoryRepository.findBySlug("meditacion-y-mindfulness").orElse(null),
                categoryRepository.findBySlug("nutricion-y-cocina").orElse(null),
                loc1,
                loc2,
                loc3
        );
    }

    private void seedCategory(String name, String description, String emoji, boolean createIfMissing) {
        String stableSlug = slugify(name);
        Category category = categoryRepository.findBySlug(stableSlug)
                .orElseGet(() -> categoryRepository.findByDescription(description).orElse(null));
        if (category == null) {
            if (!createIfMissing) {
                return;
            }
            Category newCategory = new Category();
            newCategory.setName(name);
            newCategory.setDescription(description);
            newCategory.setEmoji(emoji);
            newCategory.setActive(true);
            categoryRepository.save(newCategory);
        } else {
            boolean changed = false;
            if (!name.equals(category.getName())) {
                category.setName(name);
                changed = true;
            }
            if (!description.equals(category.getDescription())) {
                category.setDescription(description);
                changed = true;
            }
            if (!stableSlug.equals(category.getSlug())) {
                category.setSlug(stableSlug);
                changed = true;
            }
            if (category.getEmoji() == null || category.getEmoji().isBlank()) {
                category.setEmoji(emoji);
                changed = true;
            }
            if (changed) {
                categoryRepository.save(category);
            }
        }
    }

    private String slugify(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
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

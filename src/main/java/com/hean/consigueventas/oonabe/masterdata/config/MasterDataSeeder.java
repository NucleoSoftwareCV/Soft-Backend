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
                "Av. Larco 123", "LINK", "Valencia", "Valencia", true, "39.474000", "-0.380000");
        Location loc2 = seedLocation("Casa Bienestar San Isidro",
                "Av. Javier Prado 456", "LINK", "Barcelona", "Barcelona", true, "41.389000", "2.159000");
        Location loc3 = seedLocation("Cada de vista", "hola", "Acceso",
                "Madrid", "Madrid", true, "40.417000", "-3.703000");

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

    /**
     * Puntos orientativos de ciudad para las tres sedes iniciales.
     * Fuente: https://www.geonames.org/es/largest-cities-in-spain.html
     * Solo completa las semillas originales sin coordenadas; respeta ediciones posteriores.
     */
    private Location seedLocation(
            String name,
            String legacyAddress,
            String legacyReference,
            String cityName,
            String provinceName,
            boolean isActive,
            String latitude,
            String longitude) {

        Location location = locationRepository.findByName(name).orElse(null);
        City targetCity = cityRepository.findByNameAndProvince(cityName, provinceName).orElse(null);
        if (targetCity == null) {
            throw new IllegalStateException("No existe la ciudad de la ubicacion inicial: " + cityName);
        }

        boolean creating = location == null;
        if (!creating) {
            boolean originalSeed = legacyAddress.equals(location.getAddress())
                    && legacyReference.equals(location.getReference())
                    && location.getLatitude() == null
                    && location.getLongitude() == null
                    && (location.getCity() == null
                        || java.util.Objects.equals(targetCity.getId(), location.getCity().getId()));
            if (!originalSeed) {
                return location;
            }
        } else {
            location = new Location();
            location.setName(name);
            location.setIsActive(isActive);
        }

        location.setAddress("Ubicación aproximada en " + cityName
                + ". Dirección exacta pendiente de confirmar.");
        location.setReference("Punto orientativo de ciudad; confirmar la dirección con el organizador.");
        location.setCity(targetCity);
        location.setLatitude(new java.math.BigDecimal(latitude));
        location.setLongitude(new java.math.BigDecimal(longitude));
        return locationRepository.save(location);
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

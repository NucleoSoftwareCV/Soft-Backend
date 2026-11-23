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

    // Catalogue agreed by the team, including department reference entries.
    private static final String[][] PERU_CITIES = {
            {"Lima", "Lima"},
            {"Callao", "Callao"},
            {"Arequipa", "Arequipa"},
            {"Trujillo", "Trujillo"},
            {"Chiclayo", "Chiclayo"},
            {"Piura", "Piura"},
            {"Ica", "Ica"},
            {"Cusco", "Cusco"},
            {"Tacna", "Tacna"},
            {"Puno", "Puno"},
            {"Huancayo", "Huancayo"},
            {"Iquitos", "Iquitos"},
            {"Pucallpa", "Pucallpa"},
            {"Moyobamba", "Moyobamba"},
            {"Puerto Maldonado", "Puerto Maldonado"},
            {"Chachapoyas", "Chachapoyas"},
            {"Huaraz", "Huaraz"},
            {"Abancay", "Abancay"},
            {"Ayacucho", "Ayacucho"},
            {"Cajamarca", "Cajamarca"},
            {"Huancavelica", "Huancavelica"},
            {"Huánuco", "Huánuco"},
            {"Moquegua", "Moquegua"},
            {"Cerro de Pasco", "Cerro de Pasco"},
            {"Tumbes", "Tumbes"},
            {"La Libertad", "La Libertad"},
            {"Lambayeque", "Lambayeque"},
            {"Junín", "Junín"},
            {"Loreto", "Loreto"},
            {"Ucayali", "Ucayali"},
            {"San Martín", "San Martín"},
            {"Madre de Dios", "Madre de Dios"},
            {"Amazonas", "Amazonas"},
            {"Áncash", "Áncash"},
            {"Apurímac", "Apurímac"},
            {"Pasco", "Pasco"}
    };

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

        for (String[] city : PERU_CITIES) {
            seedCity(city[0], city[1]);
        }
        // Keep historical relationships, but remove obsolete country entries from public selectors.
        cityRepository.findByIsActiveTrue().stream()
                .filter(city -> "ES".equals(city.getCountryCode()))
                .forEach(city -> { city.setIsActive(false); cityRepository.save(city); });

        Location loc1 = seedLocation("Centro Holistico Miraflores",
                "Av. Larco 123", "LINK", "Miraflores", "Valencia", "39.474000", "-0.380000", "-12.121000", "-77.029000");
        Location loc2 = seedLocation("Casa Bienestar San Isidro",
                "Av. Javier Prado 456", "LINK", "San Isidro", "Barcelona", "41.389000", "2.159000", "-12.099000", "-77.035000");
        Location loc3 = seedLocation("Cada de vista", "hola", "Acceso",
                "Lima Cercado", "Madrid", "40.417000", "-3.703000", "-12.046000", "-77.043000");

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
     * District reference points in Lima, NOT verified venue addresses.
     * Only migrates untouched legacy seeds or our exact previous approximate points.
     * User-corrected addresses, coordinates and inactive status are preserved.
     */
    private Location seedLocation(
            String name, String legacyAddress, String legacyReference, String district,
            String legacyCity, String legacyLatitude, String legacyLongitude,
            String latitude, String longitude) {
        Location location = locationRepository.findByName(name).orElse(null);
        City lima = cityRepository.findByNameAndProvince("Lima", "Lima").orElseThrow();
        String address = "Ubicación aproximada en " + district
                + ", Lima. Dirección exacta pendiente de confirmar.";
        if (location != null) {
            String city = location.getCity() == null ? null : location.getCity().getName();
            boolean legacyOrTargetCity = city == null || legacyCity.equals(city) || "Lima".equals(city);
            boolean missingCoordinates = location.getLatitude() == null && location.getLongitude() == null;
            boolean rawSeed = legacyAddress.equals(location.getAddress())
                    && legacyReference.equals(location.getReference()) && missingCoordinates;
            boolean oldPoint = ("Ubicación aproximada en " + legacyCity
                    + ". Dirección exacta pendiente de confirmar.").equals(location.getAddress())
                    && "Punto orientativo de ciudad; confirmar la dirección con el organizador.".equals(location.getReference())
                    && matches(location.getLatitude(), legacyLatitude)
                    && matches(location.getLongitude(), legacyLongitude);
            if (!legacyOrTargetCity || (!rawSeed && !oldPoint)) {
                return location;
            }
        } else {
            location = new Location();
            location.setName(name);
            location.setIsActive(true);
        }
        location.setAddress(address);
        location.setReference("Punto orientativo de distrito; confirmar la dirección con el organizador.");
        location.setCity(lima);
        location.setLatitude(new java.math.BigDecimal(latitude));
        location.setLongitude(new java.math.BigDecimal(longitude));
        return locationRepository.save(location);
    }

    private boolean matches(java.math.BigDecimal actual, String expected) {
        return actual != null && actual.compareTo(new java.math.BigDecimal(expected)) == 0;
    }

    private void seedCity(String name, String province) {
        City city = cityRepository.findByNameAndProvince(name, province).orElse(null);
        if (city == null) {
            City newCity = new City();
            newCity.setName(name.trim());
            newCity.setProvince(province.trim());
            newCity.setCountryCode("PE");
            newCity.setIsActive(true);
            cityRepository.save(newCity);
        } else {
            boolean changed = false;
            if (!"PE".equals(city.getCountryCode())) {
                city.setCountryCode("PE");
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

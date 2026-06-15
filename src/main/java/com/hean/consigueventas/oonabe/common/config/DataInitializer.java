package com.hean.consigueventas.oonabe.common.config;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.location.entity.Location;
import com.hean.consigueventas.oonabe.location.repository.LocationRepository;
import com.hean.consigueventas.oonabe.user.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedBaseData(UserService userService, CategoryRepository categoryRepository, LocationRepository locationRepository) {
        return args -> {
            userService.getOrCreateRole("ROLE_USER", "Usuario final");
            userService.getOrCreateRole("ROLE_ADMIN", "Administrador del sistema");

            seedCategory(categoryRepository, "Yoga", "Practicas de yoga y bienestar corporal.");
            seedCategory(categoryRepository, "Hielo y Breathwork", "Experiencias de respiracion consciente y exposicion al frio.");
            seedCategory(categoryRepository, "Arte y Creatividad", "Actividades creativas para expresion y bienestar.");
            seedCategory(categoryRepository, "Movimiento", "Experiencias de movimiento consciente.");
            seedCategory(categoryRepository, "Deporte", "Actividades fisicas orientadas al bienestar.");
            seedCategory(categoryRepository, "Meditacion y Mindfulness", "Practicas de atencion plena y meditacion.");
            seedCategory(categoryRepository, "Sonido y Vibracion", "Experiencias de sonido, vibracion y relajacion.");
            seedCategory(categoryRepository, "Espiritualidad y Energia", "Practicas energeticas y espirituales.");
            seedCategory(categoryRepository, "Nutricion y Cocina", "Experiencias de alimentacion consciente.");
            seedCategory(categoryRepository, "Psicologia", "Acompanamiento psicologico y bienestar emocional.");
            seedCategory(categoryRepository, "Cuerpo y Salud", "Practicas centradas en salud corporal integral.");

            seedLocation(locationRepository, "Centro Holístico Miraflores", "Av. Larco 123", "LINK", false, "https://meet.google.com/");
            seedLocation(locationRepository,"Casa Bienestar San Isidro","Av. Javier Prado 456","LINK",false,"https://meet.google.com/");
            seedLocation(locationRepository,"Cada de vista","hola","Acceso",true,"https://meet.google.com/");

        };
    }

    private void seedCategory(CategoryRepository categoryRepository, String name, String description) {
        categoryRepository.findByName(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            category.setActive(true);
            return categoryRepository.save(category);
        });
    }

    private void seedLocation(
            LocationRepository locationRepository,
            String name,
            String address,
            String reference,
            boolean isActive,
            String locationLink) {

        locationRepository.findByName(name).orElseGet(() -> {
            Location location = new Location();
            location.setName(name);
            location.setAddress(address);
            location.setReference(reference);
            location.setIsActive(isActive);
            location.setLocationLink(locationLink);

            return locationRepository.save(location);
        });
    }


}

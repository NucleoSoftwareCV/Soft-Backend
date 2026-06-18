package com.hean.consigueventas.oonabe.common.config;

import com.hean.consigueventas.oonabe.catalog.entity.City;
import com.hean.consigueventas.oonabe.catalog.repository.CityRepository;
import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.location.entity.Location;
import com.hean.consigueventas.oonabe.location.repository.LocationRepository;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import com.hean.consigueventas.oonabe.user.service.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashSet;
import java.util.Set;

@Configuration
@Profile({"default", "dev", "local", "test"})
public class DataInitializer {

    @Bean
    CommandLineRunner seedBaseData(
            IUserService userService,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            LocationRepository locationRepository,
            CityRepository cityRepository
            ) {
        return args -> {
            Role roleUser = userService.getOrCreateRole("ROLE_USER", "Usuario final");
            Role roleAdmin = userService.getOrCreateRole("ROLE_ADMIN", "Administrador del sistema");

            seedUser(userRepository, "user1", "user1@oona.es", "$2a$12$UW77HqKPS52U7hJF9BCEYO9xS7SG9Y5/QsoMtpQ7fdJWiQfqeiJd2", Set.of(roleUser));
            seedUser(userRepository, "user2", "user2@oona.es", "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G", Set.of(roleUser));
            seedUser(userRepository, "admin_main1", "admin1@oona.es", "$2a$10$Mdap8zU9ZNG6oqsRUm6U7eh6Kr6oGpG.ZSRS.E8YI3bPJJC419mG2", Set.of(roleAdmin));
            seedUser(userRepository, "admin_main2", "admin2@oona.es", "$2a$10$Y3wc8XrAr4xxFCkll4Ao9er1XWddL39zVRwLBjUPhrcMUmB6SF9DC", Set.of(roleAdmin));

            seedCategory(categoryRepository, "Yoga", "Practicas de yoga y bienestar corporal.");
            seedCategory(categoryRepository, "Hielo y Breathwork", "Experiencias de respiracion consciente y exposicion al frio.");
            seedCategory(categoryRepository, "Arte y Creatividad", "Actividades creativas para expresion y bienestar.");
            seedCategory(categoryRepository, "Movimiento", "Experiencias de movimiento consciente.");
            seedCategory(categoryRepository, "Deporte", "Actividades físicas orientadas al bienestar.");
            seedCategory(categoryRepository, "Meditacion y Mindfulness", "Practicas de atencion plena y meditacion.");
            seedCategory(categoryRepository, "Sonido y Vibracion", "Experiencias de sonido, vibracion y relajacion.");
            seedCategory(categoryRepository, "Espiritualidad y Energia", "Practicas energeticas y espirituales.");
            seedCategory(categoryRepository, "Nutricion y Cocina", "Experiencias de alimentacion consciente.");
            seedCategory(categoryRepository, "Psicologia", "Acompanamiento psicologico y bienestar emocional.");
            seedCategory(categoryRepository, "Cuerpo y Salud", "Practicas centradas en salud corporal integral.");

            seedLocation(locationRepository, "Centro Holistico Miraflores", "Av. Larco 123", "LINK", false);
            seedLocation(locationRepository, "Casa Bienestar San Isidro", "Av. Javier Prado 456", "LINK", false);
            seedLocation(locationRepository, "Cada de vista", "hola", "Acceso", true);

            seedCity(cityRepository, "Madrid", "Madrid", "ES", true);
            seedCity(cityRepository, "Barcelona", "Barcelona", "ES", true);
            seedCity(cityRepository, "Valencia", "Valencia", "ES", true);
            seedCity(cityRepository, "Sevilla", "Sevilla", "ES", true);
            seedCity(cityRepository, "MÃ¡laga", "MÃ¡laga", "ES", true);
            seedCity(cityRepository, "Bilbao", "Vizcaya", "ES", true);
            seedCity(cityRepository, "Zaragoza", "Zaragoza", "ES", true);
            seedCity(cityRepository, "Palma", "Islas Baleares", "ES", true);
            seedCity(cityRepository, "Alicante", "Alicante", "ES", true);
            seedCity(cityRepository, "Granada", "Granada", "ES", true);
        };
    }

    private void seedUser(UserRepository userRepository, String username, String email, String encodedPassword, Set<Role> roles) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(encodedPassword);
            user.setRoles(new HashSet<>(roles));
            user.setActive(true);
            userRepository.save(user);
        }
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
            boolean isActive) {

        locationRepository.findByName(name).orElseGet(() -> {
            Location location = new Location();
            location.setName(name);
            location.setAddress(address);
            location.setReference(reference);
            location.setIsActive(isActive);
            return locationRepository.save(location);
        });
    }

    private void seedCity(
            CityRepository cityRepository,
            String name,
            String province,
            String countryCode,
            boolean active) {

        cityRepository.findByNameAndProvince(name, province).orElseGet(() -> {
            City city = new City();
            city.setName(name);
            city.setProvince(province);
            city.setCountryCode(countryCode);
            city.setIsActive(active);
            return cityRepository.save(city);
        });
    }


}

package com.hean.consigueventas.oonabe.common.config;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.category.repository.CategoryRepository;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.LocationRepository;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.entity.MeetingLink;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.repository.MeetingLinkRepository;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import com.hean.consigueventas.oonabe.user.service.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
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
            CityRepository cityRepository,
            SpecialistProfileRepository specialistProfileRepository,
            OneToOneServiceRepository serviceRepository,
            EventRepository eventRepository,
            EventOccurrenceRepository occurrenceRepository,
            MeetingLinkRepository meetingLinkRepository
            ) {
        return args -> {
            Role roleUser = userService.getOrCreateRole("ROLE_USER", "Usuario final");
            Role roleAdmin = userService.getOrCreateRole("ROLE_ADMIN", "Administrador del sistema");
            Role roleProfessional = userService.getOrCreateRole("ROLE_PROFESSIONAL", "Profesional / Especialista");

            seedUser(userRepository, "user1", "user1@oona.es", "$2a$12$UW77HqKPS52U7hJF9BCEYO9xS7SG9Y5/QsoMtpQ7fdJWiQfqeiJd2", Set.of(roleUser));
            seedUser(userRepository, "user2", "user2@oona.es", "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G", Set.of(roleUser));
            seedUser(userRepository, "admin_main1", "admin1@oona.es", "$2a$10$Mdap8zU9ZNG6oqsRUm6U7eh6Kr6oGpG.ZSRS.E8YI3bPJJC419mG2", Set.of(roleAdmin));
            seedUser(userRepository, "admin_main2", "admin2@oona.es", "$2a$10$Y3wc8XrAr4xxFCkll4Ao9er1XWddL39zVRwLBjUPhrcMUmB6SF9DC", Set.of(roleAdmin));

            User specUser1 = seedUser(userRepository, "specialist_ana", "ana@oona.es", "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G", Set.of(roleProfessional));
            User specUser2 = seedUser(userRepository, "specialist_carlos", "carlos@oona.es", "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G", Set.of(roleProfessional));

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

            Location loc1 = seedLocation(locationRepository, cityRepository, "Centro Holistico Miraflores", "Av. Larco 123", "LINK", "Valencia", true);
            Location loc2 = seedLocation(locationRepository, cityRepository, "Casa Bienestar San Isidro", "Av. Javier Prado 456", "LINK", "Barcelona", true);
            Location loc3 = seedLocation(locationRepository, cityRepository, "Cada de vista", "hola", "Acceso", "Madrid", true);

            SpecialistProfile profileAna = seedSpecialist(specialistProfileRepository, specUser1, "ana-psicologa", "Ana Gómez", "Psicóloga clínica con más de 10 años de experiencia.", "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2", "+34600111222", "ana@oona.es", "https://anagomez.es");
            SpecialistProfile profileCarlos = seedSpecialist(specialistProfileRepository, specUser2, "carlos-yoga", "Carlos Ruiz", "Instructor certificado de Hatha y Vinyasa Yoga.", "https://images.unsplash.com/photo-1534528741775-53994a69daeb", "+34600333444", "carlos@oona.es", "https://carlosyoga.es");

            seedOneToOneService(serviceRepository, profileAna, "Terapia Psicológica de Acompañamiento", "Sesión de terapia individual enfocada en ansiedad y manejo del estrés en la vida diaria.", 60, SessionModality.ONLINE, null, 65.00, "EUR", PublicationStatus.PUBLICADO);
            seedOneToOneService(serviceRepository, profileAna, "Evaluación de Perfil Cognitivo", "Evaluación integral de funciones cognitivas y atención para adultos mayores.", 90, SessionModality.PRESENCIAL, loc1, 120.00, "EUR", PublicationStatus.PUBLICADO);
            seedOneToOneService(serviceRepository, profileCarlos, "Clase Personalizada de Hatha Yoga", "Sesión individual adaptada a tu nivel y objetivos físicos y espirituales.", 75, SessionModality.PRESENCIAL, loc2, 50.00, "EUR", PublicationStatus.PUBLICADO);
            seedOneToOneService(serviceRepository, profileCarlos, "Asesoría de Meditación Guiada y Mindfulness", "Iniciación teórica y práctica en mindfulness y respiración consciente.", 45, SessionModality.ONLINE, null, 40.00, "EUR", PublicationStatus.PUBLICADO);
            seedOneToOneService(serviceRepository, profileCarlos, "Borrrador Clase Vinyasa Yoga", "Esta clase aún está en borrador.", 60, SessionModality.ONLINE, null, 45.00, "EUR", PublicationStatus.BORRADOR);

            // Sembrado de Categorías de referencia para Eventos
            Category catCuerpo = categoryRepository.findByName("Cuerpo y Salud").orElse(null);
            Category catMovimiento = categoryRepository.findByName("Movimiento").orElse(null);
            Category catSonido = categoryRepository.findByName("Sonido y Vibracion").orElse(null);
            Category catHielo = categoryRepository.findByName("Hielo y Breathwork").orElse(null);
            Category catYoga = categoryRepository.findByName("Yoga").orElse(null);
            Category catMeditacion = categoryRepository.findByName("Meditacion y Mindfulness").orElse(null);
            Category catNutricion = categoryRepository.findByName("Nutricion y Cocina").orElse(null);

            // 1. Taller de Porteo Ergonómico (Online)
            Event event1 = seedEvent(eventRepository,
                    "Taller de Porteo Ergonómico Fresco - Edición Verano",
                    "Aprende las opciones más frescas para portear a tu bebé cuando hace calor.",
                    "Taller de porteo ergonómico edición verano. En este taller veremos las opciones más frescas para portear a tu bebé cuando hace calor. Si estás embarazada es el mejor momento para informarte.",
                    EventModality.ONLINE, 10.00, "EUR", (short) 18, catCuerpo, profileAna,
                    EventType.TALLER, false);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event1, null,
                    Instant.parse("2026-06-29T12:00:00Z"), Instant.parse("2026-06-29T13:30:00Z"), 20,
                    "https://zoom.us/j/9876543210?pwd=secretZoomPassword123");

            // 2. Pilates Aéreo (Presencial)
            Event event2 = seedEvent(eventRepository,
                    "Pilates Aéreo. Llevas tu cuerpo a otro nivel",
                    "Una experiencia de pilates en suspensión para trabajar fuerza y flexibilidad.",
                    "Descubre los beneficios del pilates aéreo trabajando con columpios especiales. Una clase que desafía tu equilibrio y fortalece todo tu core de forma divertida y segura.",
                    EventModality.PRESENCIAL, 20.00, "EUR", (short) 16, catMovimiento, profileCarlos,
                    EventType.CLASE, true);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event2, loc1,
                    Instant.parse("2026-06-26T15:00:00Z"), Instant.parse("2026-06-26T16:30:00Z"), 15, null);

            // 3. Baño de Sonido al Atardecer (Presencial)
            Event event3 = seedEvent(eventRepository,
                    "Baño de Sonido al Atardecer en Paddle Surf",
                    "Meditación vibracional flotando sobre el agua durante la puesta de sol.",
                    "Una experiencia única que combina el equilibrio y la relajación del Paddle Surf con las vibraciones armónicas de los cuencos tibetanos y gongs al atardecer.",
                    EventModality.PRESENCIAL, 30.00, "EUR", (short) 18, catSonido, profileCarlos,
                    EventType.CEREMONIA, false);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event3, loc2,
                    Instant.parse("2026-06-26T17:30:00Z"), Instant.parse("2026-06-26T19:00:00Z"), 10, null);

            // 4. Taller de Breathwork & Hielo (Presencial)
            Event event4 = seedEvent(eventRepository,
                    "Taller de Breathwork & Hielo. Despierta tu fuego interno",
                    "Aprende técnicas avanzadas de respiración y sumérgete en tina de hielo.",
                    "Taller práctico de respiración consciente combinado con la inmersión en tina de hielo. Aprende a dominar tu mente, controlar tu sistema nervioso y potenciar tu sistema inmune.",
                    EventModality.PRESENCIAL, 45.00, "EUR", (short) 18, catHielo, profileAna,
                    EventType.TALLER, false);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event4, loc2,
                    Instant.parse("2026-06-27T10:00:00Z"), Instant.parse("2026-06-27T12:00:00Z"), 12, null);

            // 5. Clase Personalizada de Yoga Vinyasa (Presencial)
            Event event5 = seedEvent(eventRepository,
                    "Clase Especial de Yoga Vinyasa al Aire Libre",
                    "Práctica fluida y dinámica de Vinyasa Yoga conectando respiración y movimiento en la playa.",
                    "Disfruta de una sesión de Yoga Vinyasa al aire libre. Fluiremos de postura a postura guiados por la respiración para revitalizar el cuerpo y calmar la mente en un entorno natural.",
                    EventModality.PRESENCIAL, 15.00, "EUR", (short) 12, catYoga, profileCarlos,
                    EventType.CLASE, true);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event5, loc1,
                    Instant.parse("2026-06-28T09:00:00Z"), Instant.parse("2026-06-28T10:30:00Z"), 25, null);

            // 6. Iniciación a la Meditación Trascendental (Online)
            Event event6 = seedEvent(eventRepository,
                    "Iniciación a la Meditación Trascendental y del Sonido",
                    "Aprende las bases teóricas y prácticas para establecer una práctica de meditación diaria.",
                    "En este encuentro online aprenderás el origen, los beneficios científicos y las técnicas fundamentales de la meditación trascendental para reducir el ruido mental.",
                    EventModality.ONLINE, 15.00, "EUR", (short) 16, catMeditacion, profileAna,
                    EventType.ENCUENTRO_GRUPAL, false);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event6, null,
                    Instant.parse("2026-06-28T18:00:00Z"), Instant.parse("2026-06-28T19:30:00Z"), 50,
                    "https://zoom.us/j/1112223333?pwd=meditationPass456");

            // 7. Taller de Nutrición Consciente (Online)
            Event event7 = seedEvent(eventRepository,
                    "Taller de Nutrición Consciente y Batch Cooking",
                    "Organiza tus comidas de la semana comiendo sano, rico y de forma balanceada.",
                    "Aprende a planificar un menú saludable y a cocinar en un solo bloque de tiempo (batch cooking) con recetas sencillas y nutritivas para toda la semana.",
                    EventModality.ONLINE, 25.00, "EUR", (short) 18, catNutricion, profileAna,
                    EventType.TALLER, false);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event7, null,
                    Instant.parse("2026-06-30T19:00:00Z"), Instant.parse("2026-06-30T21:00:00Z"), 30,
                    "https://zoom.us/j/4445556666?pwd=nutritionPass789");

            // 8. Retiro Urbano de Mindfulness (Presencial)
            Event event8 = seedEvent(eventRepository,
                    "Retiro Urbano de Mindfulness y Naturaleza",
                    "Un día entero de desconexión y presencia plena en el parque del Retiro.",
                    "Una jornada dedicada al cultivo de la atención plena a través de caminatas conscientes, prácticas de escaneo corporal y meditaciones en grupo en medio de la naturaleza.",
                    EventModality.PRESENCIAL, 60.00, "EUR", (short) 18, catMeditacion, profileCarlos,
                    EventType.RETIRO, false);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event8, loc3,
                    Instant.parse("2026-07-02T10:00:00Z"), Instant.parse("2026-07-02T17:00:00Z"), 15, null);

            // 9. Sesión Especial de Baño de Gongs (Presencial)
            Event event9 = seedEvent(eventRepository,
                    "Sesión Especial de Baño de Gongs y Armónicos",
                    "Relajación profunda a través del sonido sagrado y las vibraciones del gong.",
                    "Sumérgete en un océano de vibraciones terapéuticas. Los gongs y los cuencos de cuarzo te guiarán a un estado meditativo profundo para restaurar tu energía vital.",
                    EventModality.PRESENCIAL, 25.00, "EUR", (short) 16, catSonido, profileCarlos,
                    EventType.CEREMONIA, true);
            seedOccurrence(occurrenceRepository, meetingLinkRepository, event9, loc1,
                    Instant.parse("2026-07-03T20:00:00Z"), Instant.parse("2026-07-03T21:30:00Z"), 20, null);
        };
    }

    private User seedUser(UserRepository userRepository, String username, String email, String encodedPassword, Set<Role> roles) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(encodedPassword);
            user.setRoles(new HashSet<>(roles));
            user.setActive(true);
            return userRepository.save(user);
        });
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

    private Location seedLocation(
            LocationRepository locationRepository,
            CityRepository cityRepository,
            String name,
            String address,
            String reference,
            String cityName,
            boolean isActive) {

        return locationRepository.findByName(name).orElseGet(() -> {
            Location location = new Location();
            location.setName(name);
            location.setAddress(address);
            location.setReference(reference);
            location.setIsActive(isActive);
            if (cityName != null) {
                location.setCity(cityRepository.findByName(cityName).orElse(null));
            }
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

    private SpecialistProfile seedSpecialist(
            SpecialistProfileRepository specialistProfileRepository,
            User user,
            String slug,
            String publicName,
            String biography,
            String photoUrl,
            String whatsappPhone,
            String publicEmail,
            String website
    ) {
        return specialistProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
            SpecialistProfile profile = new SpecialistProfile();
            profile.setUser(user);
            profile.setSlug(slug);
            profile.setPublicName(publicName);
            profile.setBiography(biography);
            profile.setPhotoUrl(photoUrl);
            profile.setWhatsappPhone(whatsappPhone);
            profile.setPublicEmail(publicEmail);
            profile.setWebsite(website);
            profile.setApprovalStatus(com.hean.consigueventas.oonabe.common.enums.ApprovalStatus.APROBADO);
            profile.setPublicationStatus(com.hean.consigueventas.oonabe.common.enums.PublicationStatus.PUBLICADO);
            return specialistProfileRepository.save(profile);
        });
    }

    private void seedOneToOneService(
            OneToOneServiceRepository serviceRepository,
            SpecialistProfile specialist,
            String title,
            String description,
            Integer durationMinutes,
            SessionModality modality,
            Location location,
            double price,
            String currency,
            PublicationStatus status
    ) {
        String tempSlug = title.toLowerCase()
                .replace("ñ", "n")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        serviceRepository.findBySlug(tempSlug).orElseGet(() -> {
            OneToOneService service = new OneToOneService();
            service.setSpecialist(specialist);
            service.setSlug(tempSlug);
            service.setTitle(title);
            service.setDescription(description);
            service.setDurationMinutes(durationMinutes);
            service.setModality(modality);
            service.setLocation(location);
            service.setPrice(java.math.BigDecimal.valueOf(price));
            service.setCurrency(currency);
            service.setStatus(status);
            return serviceRepository.save(service);
        });
    }

    private Event seedEvent(
            EventRepository eventRepository,
            String title,
            String summary,
            String description,
            EventModality modality,
            double price,
            String currency,
            Short minimumAge,
            Category category,
            SpecialistProfile specialist,
            EventType eventType,
            boolean isRecurring
    ) {
        return eventRepository.findByTitle(title).orElseGet(() -> {
            Event event = new Event();
            event.setTitle(title);
            event.setSummary(summary);
            event.setDescription(description);
            event.setModality(modality);
            event.setPriceFrom(java.math.BigDecimal.valueOf(price));
            event.setCurrency(currency);
            event.setMinimumAge(minimumAge);
            event.setStatus(EventStatus.PUBLICADO);
            event.setFeatured(true);
            event.setEventType(eventType);
            event.setRecurring(isRecurring);
            event.setCategory(category);
            event.setSpecialist(specialist);
            return eventRepository.save(event);
        });
    }

    private void seedOccurrence(
            EventOccurrenceRepository occurrenceRepository,
            MeetingLinkRepository meetingLinkRepository,
            Event event,
            Location location,
            Instant startsAt,
            Instant endsAt,
            Integer capacity,
            String meetingUrl
    ) {
        if (occurrenceRepository.findByEventIdOrderByStartsAtAsc(event.getId()).isEmpty()) {
            EventOccurrence occurrence = new EventOccurrence();
            occurrence.setEvent(event);
            occurrence.setStartsAt(startsAt);
            occurrence.setEndsAt(endsAt);
            occurrence.setCapacity(capacity);
            occurrence.setReservedSpots(0);
            occurrence.setStatus(EventOccurrenceStatus.PROGRAMADA);
            occurrence.setLocation(location);

            EventOccurrence savedOccurrence = occurrenceRepository.save(occurrence);

            if (event.getModality() == EventModality.ONLINE && meetingUrl != null) {
                MeetingLink meetingLink = new MeetingLink();
                meetingLink.setEventOccurrence(savedOccurrence);
                meetingLink.setPlatform("ZOOM");
                meetingLink.setMeetingUrl(meetingUrl);
                meetingLink.setMeetingId("123-456-789");
                meetingLink.setPassword("secret");
                meetingLinkRepository.save(meetingLink);

                savedOccurrence.setMeetingLink(meetingLink);
                occurrenceRepository.save(savedOccurrence);
            }
        }
    }
}

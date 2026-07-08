package com.hean.consigueventas.oonabe.common.config;

import com.hean.consigueventas.oonabe.event.config.EventDataSeeder;
import com.hean.consigueventas.oonabe.masterdata.config.MasterDataSeeder;
import com.hean.consigueventas.oonabe.oneToOneSession.config.OneToOneDataSeeder;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import com.hean.consigueventas.oonabe.user.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashSet;
import java.util.Set;

@Configuration
@Profile({ "default", "dev", "local", "test", "postgres" })
public class DataInitializer {

    @Bean
    CommandLineRunner seedBaseData(
            UserService userService,
            UserRepository userRepository,
            SpecialistProfileRepository specialistProfileRepository,
            EventDataSeeder eventDataSeeder,
            MasterDataSeeder masterDataSeeder,
            OneToOneDataSeeder oneToOneDataSeeder) {
        return args -> {
            Role roleUser = userService.getOrCreateRole(Role.ROLE_USER, "Usuario final");
            Role roleAdmin = userService.getOrCreateRole(Role.ROLE_ADMIN, "Administrador del sistema");
            Role roleProfessional = userService.getOrCreateRole(Role.ROLE_PROFESSIONAL,
                    "Profesional / Especialista / Centro de Salud / Organizador");

            seedUser(userRepository, "user1", "user1@oona.es",
                    "$2a$12$UW77HqKPS52U7hJF9BCEYO9xS7SG9Y5/QsoMtpQ7fdJWiQfqeiJd2", Set.of(roleUser));
            seedUser(userRepository, "user2", "user2@oona.es",
                    "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G", Set.of(roleUser));
            seedUser(userRepository, "admin_main1", "admin1@oona.es",
                    "$2a$10$Mdap8zU9ZNG6oqsRUm6U7eh6Kr6oGpG.ZSRS.E8YI3bPJJC419mG2", Set.of(roleAdmin));
            seedUser(userRepository, "admin_main2", "admin2@oona.es",
                    "$2a$10$Y3wc8XrAr4xxFCkll4Ao9er1XWddL39zVRwLBjUPhrcMUmB6SF9DC", Set.of(roleAdmin));

            User specUser1 = seedUser(userRepository, "specialist_ana", "ana@oona.es",
                    "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G", Set.of(roleProfessional));
            User specUser2 = seedUser(userRepository, "specialist_carlos", "carlos@oona.es",
                    "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G", Set.of(roleProfessional));

            MasterDataSeeder.SeedData masterData = masterDataSeeder.seed();

            SpecialistProfile profileAna = seedSpecialist(specialistProfileRepository, specUser1, "ana-psicologa",
                    "Ana Gómez", "Psicóloga clínica con más de 10 años de experiencia.",
                    "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2", "+34600111222", "ana@oona.es",
                    "https://anagomez.es");
            SpecialistProfile profileCarlos = seedSpecialist(specialistProfileRepository, specUser2, "carlos-yoga",
                    "Carlos Ruiz", "Instructor certificado de Hatha y Vinyasa Yoga.",
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb", "+34600333444", "carlos@oona.es",
                    "https://carlosyoga.es");

            oneToOneDataSeeder.seed(profileAna, profileCarlos, masterData.loc1(), masterData.loc2());
            eventDataSeeder.seed(
                    masterData.catCuerpo(),
                    masterData.catMovimiento(),
                    masterData.catSonido(),
                    masterData.catHielo(),
                    masterData.catYoga(),
                    masterData.catMeditacion(),
                    masterData.catNutricion(),
                    masterData.loc1(),
                    masterData.loc2(),
                    masterData.loc3(),
                    profileAna,
                    profileCarlos);

        };
    }

    private User seedUser(UserRepository userRepository, String username, String email, String encodedPassword,
            Set<Role> roles) {
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

    private SpecialistProfile seedSpecialist(
            SpecialistProfileRepository specialistProfileRepository,
            User user,
            String slug,
            String publicName,
            String biography,
            String photoUrl,
            String whatsappPhone,
            String publicEmail,
            String website) {
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


}

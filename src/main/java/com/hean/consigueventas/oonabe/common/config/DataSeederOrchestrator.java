package com.hean.consigueventas.oonabe.common.config;

import com.hean.consigueventas.oonabe.event.config.EventDataSeeder;
import com.hean.consigueventas.oonabe.masterdata.config.MasterDataSeeder;
import com.hean.consigueventas.oonabe.oneToOneSession.config.OneToOneDataSeeder;
import com.hean.consigueventas.oonabe.profileProfesional.config.SpecialistProfileSeeder;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.config.UserSeeder;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
@Profile({ "default", "dev", "local", "test", "postgres" })
public class DataSeederOrchestrator {

        @Bean
        CommandLineRunner seedBaseData(
                        UserSeeder userSeeder,
                        SpecialistProfileSeeder specialistProfileSeeder,
                        MasterDataSeeder masterDataSeeder,
                        OneToOneDataSeeder oneToOneDataSeeder,
                        EventDataSeeder eventDataSeeder) {
                return args -> {
                        Role roleUser = userSeeder.seedRole(Role.ROLE_USER, "Usuario final");
                        Role roleAdmin = userSeeder.seedRole(Role.ROLE_ADMIN, "Administrador del sistema");
                        Role roleProfessional = userSeeder.seedRole(Role.ROLE_PROFESSIONAL,
                                        "Profesional / Especialista / Centro de Salud / Organizador");

                        // Hashes de contraseñas de fuerza 12 conservados (raw -> hash):
                        // User1? -> $2a$12$Lg.NO/N371FWxM4Q8zLMD.ZRtyKFhZx0DdkF.pYhP5lEo8ui0T9VG
                        // User2! -> $2a$12$zfWqThYYydUBgUQ2/FGa4Oi3YMweIzonj5IMfGGskAX9igSxWDgIC
                        // Admin1@ -> $2a$12$CH2unS7WzVgwZb4NbpX/2esOGl3lqrQmnLXWOTA0B3jnisLAVrYnm
                        // Admin2# -> $2a$12$iEPmSb9Wf9moCU1kxmbVMelRk0fLZB9oEE/fq/VmVcXpos3VkfREG
                        userSeeder.seedUser("user1", "user1@oona.es",
                                        "$2a$12$Lg.NO/N371FWxM4Q8zLMD.ZRtyKFhZx0DdkF.pYhP5lEo8ui0T9VG",
                                        Set.of(roleUser));
                        userSeeder.seedUser("user2", "user2@oona.es",
                                        "$2a$12$zfWqThYYydUBgUQ2/FGa4Oi3YMweIzonj5IMfGGskAX9igSxWDgIC",
                                        Set.of(roleUser));
                        userSeeder.seedUser("admin_main1", "admin1@oona.es",
                                        "$2a$12$CH2unS7WzVgwZb4NbpX/2esOGl3lqrQmnLXWOTA0B3jnisLAVrYnm",
                                        Set.of(roleAdmin));
                        userSeeder.seedUser("admin_main2", "admin2@oona.es",
                                        "$2a$12$iEPmSb9Wf9moCU1kxmbVMelRk0fLZB9oEE/fq/VmVcXpos3VkfREG",
                                        Set.of(roleAdmin));

                        User specUser1 = userSeeder.seedUser("specialist_ana", "ana@oona.es",
                                        "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G",
                                        Set.of(roleProfessional));
                        User specUser2 = userSeeder.seedUser("specialist_carlos", "carlos@oona.es",
                                        "$2a$10$1yXne63tKNiaeGrpPN0tD.1Sq5VM.SCCcZKUN53lbz7OYA49fLa8G",
                                        Set.of(roleProfessional));
                        User professionalDemo = userSeeder.seedUser(
                                        "professional_demo",
                                        "professional_demo@oona.es",
                                        "$2a$12$3dFC2CgiIe6MloOVucl8BO/5llaccXtRPNqZXYUA/chcUoyxJd3vW",
                                        Set.of(roleUser, roleProfessional));

                        MasterDataSeeder.SeedData masterData = masterDataSeeder.seed();

                        SpecialistProfile profileAna = specialistProfileSeeder.seedSpecialist(
                                specUser1,
                                "ana-psicologa",
                                "Ana Gómez",
                                "Psicóloga clínica con más de 10 años de experiencia.",
                                "Ana acompaña a personas en procesos de bienestar emocional, brindando sesiones orientadas al autoconocimiento, la gestión emocional y el desarrollo personal.",
                                "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2",
                                "+34600111222",
                                "ana@oona.es",
                                "https://anagomez.es"
                        );

                        SpecialistProfile profileCarlos = specialistProfileSeeder.seedSpecialist(
                                specUser2,
                                "carlos-yoga",
                                "Carlos Ruiz",
                                "Instructor certificado de Hatha y Vinyasa Yoga.",
                                "Carlos guía prácticas de yoga enfocadas en movimiento consciente, respiración y equilibrio físico-emocional, adaptando cada sesión al nivel de los participantes.",
                                "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                                "+34600333444",
                                "carlos@oona.es",
                                "https://carlosyoga.es"
                        );

                        SpecialistProfile demoProfile = specialistProfileSeeder.seedSpecialist(
                                professionalDemo,
                                "profesional-demo-oona",
                                "Profesional Demo Oona",
                                "Perfil publicado para validar el portal profesional.",
                                "Cuenta de demostracion con eventos y sesiones reales asociados.",
                                "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                                "+34600999888",
                                "professional_demo@oona.es",
                                "https://oona.es"
                        );

                        oneToOneDataSeeder.seed(profileAna, profileCarlos, masterData.loc1(), masterData.loc2());
                        oneToOneDataSeeder.seedProfessionalDemo(demoProfile, masterData.loc1());
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
                        eventDataSeeder.seedProfessionalDemo(
                                        masterData.catYoga(),
                                        masterData.loc1(),
                                        demoProfile);
                };
        }
}

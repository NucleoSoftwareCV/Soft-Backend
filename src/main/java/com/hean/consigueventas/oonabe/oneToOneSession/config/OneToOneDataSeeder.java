package com.hean.consigueventas.oonabe.oneToOneSession.config;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.springframework.stereotype.Component;

@Component
public class OneToOneDataSeeder {

    private final OneToOneServiceRepository serviceRepository;

    public OneToOneDataSeeder(OneToOneServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public void seed(
            SpecialistProfile profileAna,
            SpecialistProfile profileCarlos,
            Location loc1,
            Location loc2) {
        seedOneToOneService(profileAna, "Terapia Psicológica de Acompañamiento",
                "Sesión de terapia individual enfocada en ansiedad y manejo del estrés en la vida diaria.", 60,
                SessionModality.ONLINE, null, 65.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileAna, "Evaluación de Perfil Cognitivo",
                "Evaluación integral de funciones cognitivas y atención para adultos mayores.", 90,
                SessionModality.PRESENCIAL, loc1, 120.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileCarlos, "Clase Personalizada de Hatha Yoga",
                "Sesión individual adaptada a tu nivel y objetivos físicos y espirituales.", 75,
                SessionModality.PRESENCIAL, loc2, 50.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileCarlos, "Asesoría de Meditación Guiada y Mindfulness",
                "Iniciación teórica y práctica en mindfulness y respiración consciente.", 45,
                SessionModality.ONLINE, null, 40.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileCarlos, "Borrrador Clase Vinyasa Yoga",
                "Esta clase aún está en borrador.", 60, SessionModality.ONLINE, null, 45.00, "EUR",
                PublicationStatus.BORRADOR);

        seedOneToOneService(profileAna, "Asesoria porteo ergonomico",
                "Acompanamiento individual para elegir y ajustar portabebes de forma comoda y segura.", 60,
                SessionModality.ONLINE, null, 60.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileAna,
                "Identifica la herida de infancia que condiciona tus relaciones",
                "Sesion individual para reconocer patrones emocionales y trabajarlos con herramientas terapeuticas.",
                60, SessionModality.ONLINE, null, 70.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileCarlos, "Bano de sonido para parejas - Sound Healing",
                "Experiencia personalizada de sonido y relajacion profunda para dos personas.", 120,
                SessionModality.PRESENCIAL, loc2, 80.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileAna, "Constelacion individual para desbloquear avances",
                "Sesion de acompanamiento sistemico enfocada en claridad emocional y toma de decisiones.", 60,
                SessionModality.ONLINE, null, 100.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileCarlos, "Cafe Aromatico",
                "Sesion sensorial para reconectar con presencia, respiracion y rituales de pausa consciente.", 60,
                SessionModality.PRESENCIAL, loc1, 15.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileCarlos, "ETERUM FLOW EXPERIENCE",
                "Sesion individual de movimiento consciente adaptada a tu energia y objetivos corporales.", 90,
                SessionModality.PRESENCIAL, loc2, 50.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileAna, "Descubre tu piel asesoria cosmetica fresca",
                "Asesoria personalizada para crear una rutina de cuidado facial simple y consciente.", 60,
                SessionModality.ONLINE, null, 0.00, "EUR", PublicationStatus.PUBLICADO);
        seedOneToOneService(profileAna, "Embarazo acompanamiento emocional",
                "Sesion individual para transitar embarazo y maternidad con herramientas de regulacion emocional.",
                90, SessionModality.ONLINE, null, 40.00, "EUR", PublicationStatus.PUBLICADO);
    }

    private void seedOneToOneService(
            SpecialistProfile specialist,
            String title,
            String description,
            Integer durationMinutes,
            SessionModality modality,
            Location location,
            double price,
            String currency,
            PublicationStatus status) {
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
            service.setImageUrl(defaultSessionImageUrl(title));
            service.setDurationMinutes(durationMinutes);
            service.setModality(modality);
            service.setLocation(location);
            service.setPrice(java.math.BigDecimal.valueOf(price));
            service.setCurrency(currency);
            service.setStatus(status);
            return serviceRepository.save(service);
        });
    }

    private String defaultSessionImageUrl(String title) {
        String normalizedTitle = title == null ? "" : title.toLowerCase();
        if (normalizedTitle.contains("yoga")) {
            return "https://images.unsplash.com/photo-1506126613408-eca07ce68773";
        }
        if (normalizedTitle.contains("porteo") || normalizedTitle.contains("embarazo")) {
            return "https://images.unsplash.com/photo-1492725764893-90b379c2b6e7";
        }
        if (normalizedTitle.contains("herida") || normalizedTitle.contains("constelacion")) {
            return "https://images.unsplash.com/photo-1551836022-d5d88e9218df";
        }
        if (normalizedTitle.contains("sonido")) {
            return "https://images.unsplash.com/photo-1516280440614-37939bbacd81";
        }
        if (normalizedTitle.contains("cafe")) {
            return "https://images.unsplash.com/photo-1447933601403-0c6688de566e";
        }
        if (normalizedTitle.contains("flow")) {
            return "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee";
        }
        if (normalizedTitle.contains("piel") || normalizedTitle.contains("cosmetica")) {
            return "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881";
        }
        if (normalizedTitle.contains("medit")) {
            return "https://images.unsplash.com/photo-1508672019048-805c876b67e2";
        }
        if (normalizedTitle.contains("cognitivo")) {
            return "https://images.unsplash.com/photo-1551836022-d5d88e9218df";
        }
        return "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2";
    }
}

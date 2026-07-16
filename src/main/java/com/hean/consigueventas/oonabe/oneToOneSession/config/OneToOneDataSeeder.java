package com.hean.consigueventas.oonabe.oneToOneSession.config;

import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.masterdata.repository.TechniqueRepository;
import com.hean.consigueventas.oonabe.masterdata.repository.WorkTopicRepository;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OneToOneDataSeeder {

    private final OneToOneServiceRepository serviceRepository;
    private final WorkTopicRepository workTopicRepository;
    private final TechniqueRepository techniqueRepository;

    public OneToOneDataSeeder(
            OneToOneServiceRepository serviceRepository,
            WorkTopicRepository workTopicRepository,
            TechniqueRepository techniqueRepository) {
        this.serviceRepository = serviceRepository;
        this.workTopicRepository = workTopicRepository;
        this.techniqueRepository = techniqueRepository;
    }

    @Transactional
    public void seed(
            SpecialistProfile profileAna,
            SpecialistProfile profileCarlos,
            Location loc1,
            Location loc2) {
        seedOneToOneService(profileAna, "Terapia Psicologica de Acompanamiento",
                "Sesion de terapia individual enfocada en ansiedad y manejo del estres in la vida diaria.", 60,
                SessionModality.ONLINE, null, 65.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Autoestima", "Bienestar"), Set.of("Terapia"));
        seedOneToOneService(profileAna, "Evaluacion de Perfil Cognitivo",
                "Evaluacion integral de funciones cognitivas y atencion para adultos mayores.", 90,
                SessionModality.PRESENCIAL, loc1, 120.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Resiliencia"), Set.of("Terapia"));
        seedOneToOneService(profileCarlos, "Clase Personalizada de Hatha Yoga",
                "Sesion individual adaptada a tu nivel y objetivos fisicos y espirituales.", 75,
                SessionModality.PRESENCIAL, loc2, 50.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Bienestar"), Set.of("Yoga"));
        seedOneToOneService(profileCarlos, "Asesoria de Meditacion Guiada y Mindfulness",
                "Iniciacion teorica y practica en mindfulness y respiracion consciente.", 45,
                SessionModality.ONLINE, null, 40.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Bienestar", "Resiliencia"), Set.of("Terapia"));
        seedOneToOneService(profileCarlos, "Borrrador Clase Vinyasa Yoga",
                "Esta clase aun esta en borrador.", 60, SessionModality.ONLINE, null, 45.00, "EUR",
                PublicationStatus.BORRADOR, Set.of("Bienestar"), Set.of("Yoga"));

        seedOneToOneService(profileAna, "Asesoria porteo ergonomico",
                "Acompanamiento individual para elegir y ajustar portabebes de forma comoda y segura.", 60,
                SessionModality.ONLINE, null, 60.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Bienestar"), Set.of("Terapia"));
        seedOneToOneService(profileAna,
                "Identifica la herida de infancia que condiciona tus relaciones",
                "Sesion individual para reconocer patrones emocionales y trabajarlos con herramientas terapeuticas.",
                60, SessionModality.ONLINE, null, 70.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Autoestima", "Resiliencia"), Set.of("Terapia"));
        seedOneToOneService(profileCarlos, "Bano de sonido para parejas - Sound Healing",
                "Experiencia personalizada de sonido y relajacion profunda para dos personas.", 120,
                SessionModality.PRESENCIAL, loc2, 80.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Bienestar"), Set.of("Terapia"));
        seedOneToOneService(profileAna, "Constelacion individual para desbloquear avances",
                "Sesion de acompanamiento sistemico enfocada en claridad emocional y toma de decisiones.", 60,
                SessionModality.ONLINE, null, 100.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Resiliencia"), Set.of("Terapia"));
        seedOneToOneService(profileCarlos, "Cafe Aromatico",
                "Sesion sensorial para reconectar con presencia, respiracion y rituales de pausa consciente.", 60,
                SessionModality.PRESENCIAL, loc1, 15.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Bienestar"), Set.of("Terapia"));
        seedOneToOneService(profileCarlos, "ETERUM FLOW EXPERIENCE",
                "Sesion individual de movimiento consciente adaptada a tu energia y objetivos corporales.", 90,
                SessionModality.PRESENCIAL, loc2, 50.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Motivacion", "Bienestar"), Set.of("Yoga"));
        seedOneToOneService(profileAna, "Descubre tu piel asesoria cosmetica fresca",
                "Asesoria personalizada para crear una rutina de cuidado facial simple y consciente.", 60,
                SessionModality.ONLINE, null, 0.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Bienestar"), Set.of("Terapia"));
        seedOneToOneService(profileAna, "Embarazo acompanamiento emocional",
                "Sesion individual para transitar embarazo y maternidad con herramientas de regulacion emocional.",
                90, SessionModality.ONLINE, null, 40.00, "EUR", PublicationStatus.PUBLICADO,
                Set.of("Autoestima", "Bienestar"), Set.of("Terapia"));
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
            PublicationStatus status,
            Set<String> workTopicNames,
            Set<String> techniqueNames) {
        String slug = slugify(title);

        OneToOneService service = serviceRepository.findBySlug(slug).orElse(null);
        Set<WorkTopic> targetWorkTopics = resolveWorkTopics(workTopicNames);
        Set<Technique> targetTechniques = resolveTechniques(techniqueNames);
        BigDecimal targetPrice = BigDecimal.valueOf(price);

        if (service == null) {
            OneToOneService newService = new OneToOneService();
            newService.setSpecialist(specialist);
            newService.setSlug(slug);
            newService.setTitle(title);
            newService.setDescription(description);
            newService.setImageUrl(defaultSessionImageUrl(title));
            newService.setDurationMinutes(durationMinutes);
            newService.setModality(modality);
            newService.setLocation(location);
            newService.setPrice(targetPrice);
            newService.setCurrency(currency);
            newService.setStatus(status);
            newService.setWorkTopics(targetWorkTopics);
            newService.setTechniques(targetTechniques);
            serviceRepository.save(newService);
        } else {
            boolean changed = false;
            if (!specialist.getId().equals(service.getSpecialist().getId())) { service.setSpecialist(specialist); changed = true; }
            if (!title.equals(service.getTitle())) { service.setTitle(title); changed = true; }
            if (!description.equals(service.getDescription())) { service.setDescription(description); changed = true; }
            if (!durationMinutes.equals(service.getDurationMinutes())) { service.setDurationMinutes(durationMinutes); changed = true; }
            if (modality != service.getModality()) { service.setModality(modality); changed = true; }
            if ((location == null && service.getLocation() != null) || (location != null && (service.getLocation() == null || !location.getId().equals(service.getLocation().getId())))) {
                service.setLocation(location);
                changed = true;
            }
            if (targetPrice.compareTo(service.getPrice()) != 0) { service.setPrice(targetPrice); changed = true; }
            if (!currency.equals(service.getCurrency())) { service.setCurrency(currency); changed = true; }
            if (status != service.getStatus()) { service.setStatus(status); changed = true; }

            Set<String> currentWorkTopics = service.getWorkTopics().stream().map(WorkTopic::getName).collect(Collectors.toSet());
            if (!workTopicNames.equals(currentWorkTopics)) {
                service.setWorkTopics(targetWorkTopics);
                changed = true;
            }
            Set<String> currentTechniques = service.getTechniques().stream().map(Technique::getName).collect(Collectors.toSet());
            if (!techniqueNames.equals(currentTechniques)) {
                service.setTechniques(targetTechniques);
                changed = true;
            }

            if (changed) {
                serviceRepository.save(service);
            }
        }
    }

    private Set<WorkTopic> resolveWorkTopics(Set<String> names) {
        return names.stream()
                .map(name -> workTopicRepository.findByNameIgnoreCase(name)
                        .orElseThrow(() -> new IllegalStateException("Tema de trabajo no encontrado: " + name)))
                .collect(Collectors.toSet());
    }

    private Set<Technique> resolveTechniques(Set<String> names) {
        return names.stream()
                .map(name -> techniqueRepository.findByNameIgnoreCase(name)
                        .orElseThrow(() -> new IllegalStateException("Tecnica no encontrada: " + name)))
                .collect(Collectors.toSet());
    }

    private String slugify(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
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

package com.hean.consigueventas.oonabe.event.config;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.entity.MeetingLink;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.repository.MeetingLinkRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class EventDataSeeder {

    private final EventRepository eventRepository;
    private final EventOccurrenceRepository occurrenceRepository;
    private final MeetingLinkRepository meetingLinkRepository;

    public EventDataSeeder(
            EventRepository eventRepository,
            EventOccurrenceRepository occurrenceRepository,
            MeetingLinkRepository meetingLinkRepository) {
        this.eventRepository = eventRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.meetingLinkRepository = meetingLinkRepository;
    }

    public void seed(
            Category catCuerpo,
            Category catMovimiento,
            Category catSonido,
            Category catHielo,
            Category catYoga,
            Category catMeditacion,
            Category catNutricion,
            Location loc1,
            Location loc2,
            Location loc3,
            SpecialistProfile profileAna,
            SpecialistProfile profileCarlos) {

        Event event1 = seedEvent(
                "Taller de Porteo Ergonómico Fresco - Edición Verano",
                "Aprende las opciones más frescas para portear a tu bebé cuando hace calor.",
                "Taller de porteo ergonómico edición verano. En este taller veremos las opciones más frescas para portear a tu bebé cuando hace calor. Si estás embarazada es el mejor momento para informarte.",
                EventModality.ONLINE, 10.00, "EUR", (short) 18, catCuerpo, profileAna,
                EventType.TALLER, false);
        seedOccurrence(event1, null,
                Instant.parse("2026-06-29T12:00:00Z"), Instant.parse("2026-06-29T13:30:00Z"), 20,
                "https://zoom.us/j/9876543210?pwd=secretZoomPassword123");
        seedEventDetailSections(event1,
                List.of("Material de apoyo digital", "Guia basica de porteo ergonomico"),
                List.of("Porteo seguro", "Apto para embarazo"),
                List.of("Muneco o portabebes si ya tienes uno"));

        Event event2 = seedEvent(
                "Pilates Aéreo. Llevas tu cuerpo a otro nivel",
                "Una experiencia de pilates en suspensión para trabajar fuerza y flexibilidad.",
                "Descubre los beneficios del pilates aéreo trabajando con columpios especiales. Una clase que desafía tu equilibrio y fortalece todo tu core de forma divertida y segura.",
                EventModality.PRESENCIAL, 20.00, "EUR", (short) 16, catMovimiento, profileCarlos,
                EventType.CLASE, true);
        seedOccurrence(event2, loc1,
                Instant.parse("2026-06-26T15:00:00Z"), Instant.parse("2026-06-26T16:30:00Z"), 15, null);
        seedEventDetailSections(event2,
                List.of("Uso de columpios de pilates aereo"),
                List.of("Movimiento", "Fuerza y flexibilidad"),
                List.of("Ropa comoda", "Botella de agua"));

        Event event3 = seedEvent(
                "Baño de Sonido al Atardecer en Paddle Surf",
                "Meditación vibracional flotando sobre el agua durante la puesta de sol.",
                "Una experiencia única que combina el equilibrio y la relajación del Paddle Surf con las vibraciones armónicas de los cuencos tibetanos y gongs al atardecer.",
                EventModality.PRESENCIAL, 30.00, "EUR", (short) 18, catSonido, profileCarlos,
                EventType.CEREMONIA, false);
        seedOccurrence(event3, loc2,
                Instant.parse("2026-06-26T17:30:00Z"), Instant.parse("2026-06-26T19:00:00Z"), 10, null);
        seedEventDetailSections(event3,
                List.of(),
                List.of("Sonido terapeutico", "Experiencia al atardecer"),
                List.of("Toalla", "Ropa que pueda mojarse"));

        Event event4 = seedEvent(
                "Taller de Breathwork & Hielo. Despierta tu fuego interno",
                "Aprende técnicas avanzadas de respiración y sumérgete en tina de hielo.",
                "Taller práctico de respiración consciente combinado con la inmersión en tina de hielo. Aprende a dominar tu mente, controlar tu sistema nervioso y potenciar tu sistema inmune.",
                EventModality.PRESENCIAL, 45.00, "EUR", (short) 18, catHielo, profileAna,
                EventType.TALLER, false);
        seedOccurrence(event4, loc2,
                Instant.parse("2026-06-27T10:00:00Z"), Instant.parse("2026-06-27T12:00:00Z"), 12, null);
        seedEventDetailSections(event4,
                List.of("Acompanamiento guiado", "Inmersion en hielo"),
                List.of("Breathwork", "Regulacion del sistema nervioso"),
                List.of("Banador", "Toalla", "Ropa de abrigo"));

        Event event5 = seedEvent(
                "Clase Especial de Yoga Vinyasa al Aire Libre",
                "Práctica fluida y dinámica de Vinyasa Yoga conectando respiración y movimiento en la playa.",
                "Disfruta de una sesión de Yoga Vinyasa al aire libre. Fluiremos de postura a postura guiados por la respiración para revitalizar el cuerpo y calmar la mente en un entorno natural.",
                EventModality.PRESENCIAL, 15.00, "EUR", (short) 12, catYoga, profileCarlos,
                EventType.CLASE, true);
        seedOccurrence(event5, loc1,
                Instant.parse("2026-06-28T09:00:00Z"), Instant.parse("2026-06-28T10:30:00Z"), 25, null);
        seedEventDetailSections(event5,
                List.of("Material para practicar yoga"),
                List.of("Yoga"),
                List.of("Ropa comoda"));

        Event event6 = seedEvent(
                "Iniciación a la Meditación Trascendental y del Sonido",
                "Aprende las bases teóricas y prácticas para establecer una práctica de meditación diaria.",
                "En este encuentro online aprenderás el origen, los beneficios científicos y las técnicas fundamentales de la meditación trascendental para reducir el ruido mental.",
                EventModality.ONLINE, 15.00, "EUR", (short) 16, catMeditacion, profileAna,
                EventType.ENCUENTRO_GRUPAL, false);
        seedOccurrence(event6, null,
                Instant.parse("2026-06-28T18:00:00Z"), Instant.parse("2026-06-28T19:30:00Z"), 50,
                "https://zoom.us/j/1112223333?pwd=meditationPass456");
        seedEventDetailSections(event6,
                List.of("Acceso a la sesion online"),
                List.of("Meditacion", "Practica para principiantes"),
                List.of());

        Event event7 = seedEvent(
                "Taller de Nutrición Consciente y Batch Cooking",
                "Organiza tus comidas de la semana comiendo sano, rico y de forma balanceada.",
                "Aprende a planificar un menú saludable y a cocinar en un solo bloque de tiempo (batch cooking) con recetas sencillas y nutritivas para toda la semana.",
                EventModality.ONLINE, 25.00, "EUR", (short) 18, catNutricion, profileAna,
                EventType.TALLER, false);
        seedOccurrence(event7, null,
                Instant.parse("2026-06-30T19:00:00Z"), Instant.parse("2026-06-30T21:00:00Z"), 30,
                "https://zoom.us/j/4445556666?pwd=nutritionPass789");

        Event event8 = seedEvent(
                "Retiro Urbano de Mindfulness y Naturaleza",
                "Un día entero de desconexión y presencia plena en el parque del Retiro.",
                "Una jornada dedicada al cultivo de la atención plena a través de caminatas conscientes, prácticas de escaneo corporal y meditaciones en grupo en medio de la naturaleza.",
                EventModality.PRESENCIAL, 60.00, "EUR", (short) 18, catMeditacion, profileCarlos,
                EventType.RETIRO, false);
        seedOccurrence(event8, loc3,
                Instant.parse("2026-07-02T10:00:00Z"), Instant.parse("2026-07-02T17:00:00Z"), 15, null);

        Event event9 = seedEvent(
                "Sesión Especial de Baño de Gongs y Armónicos",
                "Relajación profunda a través del sonido sagrado y las vibraciones del gong.",
                "Sumérgete en un océano de vibraciones terapéuticas. Los gongs y los cuencos de cuarzo te guiarán a un estado meditativo profundo para restaurar tu energía vital.",
                EventModality.PRESENCIAL, 25.00, "EUR", (short) 16, catSonido, profileCarlos,
                EventType.CEREMONIA, true);
        seedOccurrence(event9, loc1,
                Instant.parse("2026-07-03T20:00:00Z"), Instant.parse("2026-07-03T21:30:00Z"), 20, null);

        Event event10 = seedEvent(
                "Yoga Restaurativo para Soltar Tension",
                "Practica suave de yoga restaurativo para relajar cuerpo y mente.",
                "Una sesion pausada con posturas sostenidas, respiracion consciente y cierre meditativo para liberar tension acumulada.",
                EventModality.PRESENCIAL, 18.00, "EUR", (short) 14, catYoga, profileAna,
                EventType.CLASE, false);
        seedOccurrence(event10, loc2,
                Instant.parse("2026-07-04T09:00:00Z"), Instant.parse("2026-07-04T10:15:00Z"), 18, null);
        seedEventDetailSections(event10,
                List.of("Bloques y mantas de apoyo"),
                List.of("Yoga", "Relajacion profunda"),
                List.of("Ropa comoda"));

        Event event11 = seedEvent(
                "Viaje Sonoro con Cuencos de Cuarzo",
                "Experiencia de sonido meditativo para descanso y claridad.",
                "Encuentro de escucha profunda con cuencos de cuarzo, respiracion guiada y relajacion corporal.",
                EventModality.PRESENCIAL, 22.00, "EUR", (short) 16, catSonido, profileAna,
                EventType.CEREMONIA, false);
        seedOccurrence(event11, loc2,
                Instant.parse("2026-07-04T19:00:00Z"), Instant.parse("2026-07-04T20:15:00Z"), 16, null);
        seedEventDetailSections(event11,
                List.of("Material de relajacion en sala"),
                List.of("Sonido terapeutico"),
                List.of("Ropa abrigada"));

        Event event12 = seedEvent(
                "Movimiento Postnatal y Cuidado Corporal",
                "Clase suave para reconectar con el cuerpo despues del embarazo.",
                "Practicaremos movilidad, respiracion y ejercicios conscientes para recuperar confianza corporal de forma segura.",
                EventModality.PRESENCIAL, 20.00, "EUR", (short) 18, catCuerpo, profileCarlos,
                EventType.CLASE, false);
        seedOccurrence(event12, loc1,
                Instant.parse("2026-07-05T11:00:00Z"), Instant.parse("2026-07-05T12:15:00Z"), 12, null);
        seedEventDetailSections(event12,
                List.of("Material de practica"),
                List.of("Cuerpo y salud", "Movimiento consciente"),
                List.of("Ropa comoda", "Agua"));

        Event event13 = seedEvent(
                "Danza Consciente para Liberar Energia",
                "Movimiento guiado para desbloquear tension y activar vitalidad.",
                "Una experiencia de movimiento libre con pautas sencillas para conectar con ritmo, respiracion y presencia.",
                EventModality.PRESENCIAL, 24.00, "EUR", (short) 16, catMovimiento, profileAna,
                EventType.CLASE, false);
        seedOccurrence(event13, loc3,
                Instant.parse("2026-07-05T17:00:00Z"), Instant.parse("2026-07-05T18:30:00Z"), 20, null);
        seedEventDetailSections(event13,
                List.of("Playlist guiada", "Espacio de integracion"),
                List.of("Movimiento", "Expresion corporal"),
                List.of("Ropa comoda"));
    }

    private Event seedEvent(
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
            boolean isRecurring) {
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

    private void seedEventDetailSections(
            Event event,
            List<String> includes,
            List<String> highlights,
            List<String> whatToBring) {
        boolean changed = false;

        if (!includes.isEmpty()) {
            event.setIncludes(new java.util.ArrayList<>(includes));
            changed = true;
        }
        if (!highlights.isEmpty()) {
            event.setHighlights(new java.util.ArrayList<>(highlights));
            changed = true;
        }
        if (!whatToBring.isEmpty()) {
            event.setWhatToBring(new java.util.ArrayList<>(whatToBring));
            changed = true;
        }

        if (changed) {
            eventRepository.save(event);
        }
    }

    private void seedOccurrence(
            Event event,
            Location location,
            Instant startsAt,
            Instant endsAt,
            Integer capacity,
            String meetingUrl
    ) {
        if (!occurrenceRepository.existsByEventId(event.getId())) {
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

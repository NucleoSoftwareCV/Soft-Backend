package com.hean.consigueventas.oonabe.event.config;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import com.hean.consigueventas.oonabe.common.enums.ImageFormat;
import com.hean.consigueventas.oonabe.common.config.TimeConfig;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventImage;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.entity.MeetingLink;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.repository.EventImageRepository;
import com.hean.consigueventas.oonabe.event.repository.MeetingLinkRepository;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class EventDataSeeder {

    private final EventRepository eventRepository;
    private final EventOccurrenceRepository occurrenceRepository;
    private final MeetingLinkRepository meetingLinkRepository;
    private final EventImageRepository eventImageRepository;
    private final Clock clock;

    public EventDataSeeder(
            EventRepository eventRepository,
            EventOccurrenceRepository occurrenceRepository,
            MeetingLinkRepository meetingLinkRepository,
            EventImageRepository eventImageRepository,
            Clock clock) {
        this.eventRepository = eventRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.meetingLinkRepository = meetingLinkRepository;
        this.eventImageRepository = eventImageRepository;
        this.clock = clock;
    }

    @Transactional
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
                futureInstant(1, 12, 0), futureInstant(1, 13, 30), 20,
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
                futureInstant(2, 15, 0), futureInstant(2, 16, 30), 15, null);
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
                futureInstant(3, 17, 30), futureInstant(3, 19, 0), 10, null);
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
        seedOccurrence(event4, loc1,
                futureInstant(4, 10, 0), futureInstant(4, 12, 0), 12, null);
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
                futureInstant(5, 9, 0), futureInstant(5, 10, 30), 25, null);
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
                futureInstant(1, 18, 0), futureInstant(1, 19, 30), 50,
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
                futureInstant(2, 19, 0), futureInstant(2, 21, 0), 30,
                "https://zoom.us/j/4445556666?pwd=nutritionPass789");

        Event event8 = seedEvent(
                "Retiro Urbano de Mindfulness y Naturaleza",
                "Un día entero de desconexión y presencia plena en el parque del Retiro.",
                "Una jornada dedicada al cultivo de la atención plena a través de caminatas conscientes, prácticas de escaneo corporal y meditaciones en grupo en medio de la naturaleza.",
                EventModality.PRESENCIAL, 60.00, "EUR", (short) 18, catMeditacion, profileCarlos,
                EventType.RETIRO, false);
        seedOccurrence(event8, loc1,
                futureInstant(3, 10, 0), futureInstant(3, 17, 0), 15, null);

        Event event9 = seedEvent(
                "Sesión Especial de Baño de Gongs y Armónicos",
                "Relajación profunda a través del sonido sagrado y las vibraciones del gong.",
                "Sumérgete en un océano de vibraciones terapéuticas. Los gongs y los cuencos de cuarzo te guiarán a un estado meditativo profundo para restaurar tu energía vital.",
                EventModality.PRESENCIAL, 25.00, "EUR", (short) 16, catSonido, profileCarlos,
                EventType.CEREMONIA, true);
        seedOccurrence(event9, loc1,
                futureInstant(4, 20, 0), futureInstant(4, 21, 30), 20, null);

        Event event10 = seedEvent(
                "Yoga Restaurativo para Soltar Tension",
                "Practica suave de yoga restaurativo para relajar cuerpo y mente.",
                "Una sesion pausada con posturas sostenidas, respiracion consciente y cierre meditativo para liberar tension acumulada.",
                EventModality.PRESENCIAL, 18.00, "EUR", (short) 14, catYoga, profileAna,
                EventType.CLASE, false);
        seedOccurrence(event10, loc2,
                futureInstant(5, 9, 0), futureInstant(5, 10, 15), 18, null);
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
                futureInstant(6, 19, 0), futureInstant(6, 20, 15), 16, null);
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
                futureInstant(7, 11, 0), futureInstant(7, 12, 15), 12, null);
        seedEventDetailSections(event12,
                List.of("Material de practica"),
                List.of("Cuerpo y salud", "Movimiento consciente"),
                List.of("Ropa comoda", "Agua"));

        Event event13 = seedEvent(
                "Danza Consciente para Liberar Energia",
                "Movimiento guiado para desbloquear tension and activar vitalidad.",
                "Una experiencia de movimiento libre con pautas sencillas para conectar con ritmo, respiracion y presencia.",
                EventModality.PRESENCIAL, 24.00, "EUR", (short) 16, catMovimiento, profileAna,
                EventType.CLASE, false);
        seedOccurrence(event13, loc3,
                futureInstant(8, 17, 0), futureInstant(8, 18, 30), 20, null);
        seedEventDetailSections(event13,
                List.of("Playlist guiada", "Espacio de integracion"),
                List.of("Movimiento", "Expresion corporal"),
                List.of("Ropa comoda"));

        seedCover(event1, "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b");
        seedCover(event2, "https://images.unsplash.com/photo-1518611012118-696072aa579a");
        seedCover(event3, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee");
        seedCover(event4, "https://images.unsplash.com/photo-1591228127791-8e2eaef098d3");
        seedCover(event5, "https://images.unsplash.com/photo-1506126613408-eca07ce68773");
        seedCover(event6, "https://images.unsplash.com/photo-1508672019048-805c876b67e2");
        seedCover(event7, "https://images.unsplash.com/photo-1490645935967-10de6ba17061");
        seedCover(event8, "https://images.unsplash.com/photo-1441974231531-c6227db76b6e");
        seedCover(event9, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819");
        seedCover(event10, "https://images.unsplash.com/photo-1545389336-cf090694435e");
        seedCover(event11, "https://images.unsplash.com/photo-1511379938547-c1f69419868d");
        seedCover(event12, "https://images.unsplash.com/photo-1555252333-9f8e92e65df9");
        seedCover(event13, "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4");
    }

    @Transactional
    public void seedProfessionalDemo(
            Category category,
            Location location,
            SpecialistProfile profile) {
        Event event = seedEvent(
                "Encuentro demo de bienestar consciente",
                "Evento publicado para probar la gestion desde el portal profesional.",
                "Encuentro practico de demostracion con movimiento suave, respiracion y cierre consciente.",
                EventModality.PRESENCIAL,
                35.00,
                "EUR",
                (short) 18,
                category,
                profile,
                EventType.TALLER,
                false);
        seedOccurrence(
                event,
                location,
                futureInstant(10, 18, 0),
                futureInstant(10, 20, 0),
                18,
                null);
        seedCover(event, "https://images.unsplash.com/photo-1506126613408-eca07ce68773");
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
        Event event = eventRepository.findByTitle(title).orElse(null);
        BigDecimal targetPrice = BigDecimal.valueOf(price);
        if (event == null) {
            Event newEvent = new Event();
            newEvent.setTitle(title);
            newEvent.setSummary(summary);
            newEvent.setDescription(description);
            newEvent.setModality(modality);
            newEvent.setPriceFrom(targetPrice);
            newEvent.setCurrency(currency);
            newEvent.setMinimumAge(minimumAge);
            newEvent.setStatus(EventStatus.PUBLICADO);
            newEvent.setFeatured(true);
            newEvent.setEventType(eventType);
            newEvent.setRecurring(isRecurring);
            newEvent.setCategory(category);
            newEvent.setSpecialist(specialist);
            return eventRepository.save(newEvent);
        } else {
            boolean changed = false;
            if (!summary.equals(event.getSummary())) { event.setSummary(summary); changed = true; }
            if (!description.equals(event.getDescription())) { event.setDescription(description); changed = true; }
            if (modality != event.getModality()) { event.setModality(modality); changed = true; }
            if (targetPrice.compareTo(event.getPriceFrom()) != 0) { event.setPriceFrom(targetPrice); changed = true; }
            if (!currency.equals(event.getCurrency())) { event.setCurrency(currency); changed = true; }
            if (!minimumAge.equals(event.getMinimumAge())) { event.setMinimumAge(minimumAge); changed = true; }
            if (event.getStatus() != EventStatus.PUBLICADO) { event.setStatus(EventStatus.PUBLICADO); changed = true; }
            if (eventType != event.getEventType()) { event.setEventType(eventType); changed = true; }
            if (isRecurring != event.isRecurring()) { event.setRecurring(isRecurring); changed = true; }
            if (category != null && (event.getCategory() == null || !category.getId().equals(event.getCategory().getId()))) { event.setCategory(category); changed = true; }
            if (specialist != null && (event.getSpecialist() == null || !specialist.getId().equals(event.getSpecialist().getId()))) { event.setSpecialist(specialist); changed = true; }
            if (changed) {
                return eventRepository.save(event);
            }
            return event;
        }
    }

    private void seedEventDetailSections(
            Event event,
            List<String> includes,
            List<String> highlights,
            List<String> whatToBring) {
        boolean changed = false;

        if (event.getIncludes() == null || !event.getIncludes().equals(includes)) {
            event.setIncludes(new java.util.ArrayList<>(includes));
            changed = true;
        }
        if (event.getHighlights() == null || !event.getHighlights().equals(highlights)) {
            event.setHighlights(new java.util.ArrayList<>(highlights));
            changed = true;
        }
        if (event.getWhatToBring() == null || !event.getWhatToBring().equals(whatToBring)) {
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
        EventOccurrence occurrence = occurrenceRepository.findFirstByEventId(event.getId()).orElse(null);
        if (occurrence == null) {
            EventOccurrence newOccurrence = new EventOccurrence();
            newOccurrence.setEvent(event);
            newOccurrence.setStartsAt(startsAt);
            newOccurrence.setEndsAt(endsAt);
            newOccurrence.setCapacity(capacity);
            newOccurrence.setReservedSpots(0);
            newOccurrence.setStatus(EventOccurrenceStatus.PROGRAMADA);
            newOccurrence.setLocation(location);
            EventOccurrence savedOccurrence = occurrenceRepository.save(newOccurrence);

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
        } else {
            boolean changed = false;
            if (!startsAt.equals(occurrence.getStartsAt())) { occurrence.setStartsAt(startsAt); changed = true; }
            if (!endsAt.equals(occurrence.getEndsAt())) { occurrence.setEndsAt(endsAt); changed = true; }
            if (!capacity.equals(occurrence.getCapacity())) { occurrence.setCapacity(capacity); changed = true; }
            if ((location == null && occurrence.getLocation() != null) || (location != null && (occurrence.getLocation() == null || !location.getId().equals(occurrence.getLocation().getId())))) {
                occurrence.setLocation(location);
                changed = true;
            }
            EventOccurrence savedOccurrence = occurrence;
            if (changed) {
                savedOccurrence = occurrenceRepository.save(occurrence);
            }

            if (event.getModality() == EventModality.ONLINE && meetingUrl != null) {
                MeetingLink meetingLink = meetingLinkRepository.findByEventOccurrenceId(savedOccurrence.getId()).orElse(null);
                if (meetingLink == null) {
                    meetingLink = new MeetingLink();
                    meetingLink.setEventOccurrence(savedOccurrence);
                    meetingLink.setPlatform("ZOOM");
                    meetingLink.setMeetingUrl(meetingUrl);
                    meetingLink.setMeetingId("123-456-789");
                    meetingLink.setPassword("secret");
                    meetingLinkRepository.save(meetingLink);

                    savedOccurrence.setMeetingLink(meetingLink);
                    occurrenceRepository.save(savedOccurrence);
                } else {
                    if (!meetingUrl.equals(meetingLink.getMeetingUrl())) {
                        meetingLink.setMeetingUrl(meetingUrl);
                        meetingLinkRepository.save(meetingLink);
                    }
                }
            }
        }
    }

    private Instant futureInstant(int daysFromToday, int hour, int minute) {
        return LocalDate.now(clock)
                .plusDays(daysFromToday)
                .atTime(LocalTime.of(hour, minute))
                .atZone(TimeConfig.BUSINESS_ZONE)
                .toInstant();
    }

    private void seedCover(Event event, String url) {
        EventImage image = eventImageRepository
                .findFirstByEventIdOrderByCoverDescSortOrderAscIdAsc(event.getId())
                .orElseGet(EventImage::new);
        image.setEvent(event);
        image.setUrl(url);
        image.setAlternativeText(event.getTitle());
        image.setCover(true);
        image.setFormat(ImageFormat.JPG);
        image.setSortOrder((short) 0);
        eventImageRepository.save(image);
    }
}

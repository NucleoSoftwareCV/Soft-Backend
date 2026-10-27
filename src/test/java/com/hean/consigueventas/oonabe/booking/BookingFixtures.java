package com.hean.consigueventas.oonabe.booking;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.event.entity.*;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;

final class BookingFixtures {
    record Fixture(User professional, User client, EventOccurrence occurrence) {}

    static Fixture create(EntityManager em, String suffix) {
        User professional = user(em, "pro-" + suffix);
        User client = user(em, "cli-" + suffix);
        CustomerProfile customer = new CustomerProfile();
        customer.setUser(client); customer.setFirstNames("Cliente"); customer.setLastNames(suffix);
        em.persist(customer);
        SpecialistProfile specialist = new SpecialistProfile();
        specialist.setUser(professional); specialist.setSlug("booking-" + suffix);
        specialist.setPublicName("Profesional " + suffix); specialist.setWhatsappPhone("+34600000000");
        specialist.setApprovalStatus(ApprovalStatus.APROBADO); specialist.setPublicationStatus(PublicationStatus.PUBLICADO);
        em.persist(specialist);
        Category category = new Category(); category.setName("Reservas " + suffix); em.persist(category);
        Event event = new Event();
        event.setTitle("Evento " + suffix); event.setDescription("Evento para verificar reservas");
        event.setCategory(category); event.setSpecialist(specialist); event.setModality(EventModality.ONLINE);
        event.setPriceFrom(new BigDecimal("25.00")); event.setStatus(EventStatus.PUBLICADO);
        event.setPaymentMethod(EventPaymentMethod.WHATSAPP); em.persist(event);
        EventOccurrence occurrence = new EventOccurrence();
        occurrence.setEvent(event); occurrence.setCapacity(10); occurrence.setReservedSpots(0);
        occurrence.setStartsAt(Instant.now().plusSeconds(86400));
        occurrence.setEndsAt(Instant.now().plusSeconds(90000));
        occurrence.setStatus(EventOccurrenceStatus.PROGRAMADA); em.persist(occurrence); em.flush();
        return new Fixture(professional, client, occurrence);
    }

    private static User user(EntityManager em, String username) {
        User user = new User(); user.setUsername(username); user.setEmail(username + "@example.com");
        em.persist(user); return user;
    }
}

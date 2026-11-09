package com.hean.consigueventas.oonabe.appointment;

import com.hean.consigueventas.oonabe.booking.entity.SessionBooking;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.*;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import java.time.*;
import java.util.List;

@Repository
public class AppointmentRepository {
    @PersistenceContext EntityManager em;
    public SpecialistProfile professional(Long userId, boolean lock) {
        var query=em.createQuery("select p from SpecialistProfile p where p.user.id=:id", SpecialistProfile.class).setParameter("id",userId);
        if(lock) query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return query.getResultStream().findFirst().orElseThrow(()->new ResourceNotFoundException("No tienes un perfil profesional."));
    }
    public CustomerProfile customer(Long userId) {
        return em.createQuery("select c from CustomerProfile c where c.user.id=:id", CustomerProfile.class)
            .setParameter("id",userId).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultStream().findFirst()
            .orElseThrow(()->new IllegalArgumentException("Completa tu perfil de cliente antes de solicitar una cita."));
    }
    public void lock(SpecialistProfile p) { em.lock(p,LockModeType.PESSIMISTIC_WRITE); }
    public <T> T find(Class<T> type, Long id) {
        if(id==null) throw new ResourceNotFoundException("Recurso no encontrado.");
        T entity=em.find(type,id);
        if(entity==null) throw new ResourceNotFoundException("Recurso no encontrado.");
        return entity;
    }
    public void save(Object entity) { em.persist(entity); em.flush(); }
    public void remove(Object entity) { em.remove(entity); }
    public List<ProfessionalAvailability> rules(Long specialistId) {
        return em.createQuery("select a from ProfessionalAvailability a left join fetch a.location where a.specialist.id=:id and a.active=true order by a.dayOfWeek,a.startTime,a.id",ProfessionalAvailability.class)
            .setParameter("id",specialistId).getResultList();
    }
    public List<ScheduleBlock> blocks(Long id, Instant start, Instant end) {
        return em.createQuery("select b from ScheduleBlock b where b.specialist.id=:id and b.startsAt<:end and b.endsAt>:start",ScheduleBlock.class)
            .setParameter("id",id).setParameter("start",start).setParameter("end",end).getResultList();
    }
    public List<SessionBooking> busy(Long professionalId, Long customerId, Instant start, Instant end, Instant now,
                                     Long excludedBookingId) {
        return em.createQuery("""
            select b from SessionBooking b where (b.specialist.id=:professionalId or b.customer.id=:customerId)
            and b.startsAt<:end and b.endsAt>:start
            and (:excludedBookingId is null or b.id<>:excludedBookingId)
            and (b.status=:confirmed or (b.status=:pending and (b.expiresAt is null or b.expiresAt>:now)))
            """,SessionBooking.class).setParameter("professionalId",professionalId).setParameter("customerId",customerId)
            .setParameter("start",start).setParameter("end",end).setParameter("now",now)
            .setParameter("excludedBookingId",excludedBookingId)
            .setParameter("confirmed",BookingStatus.CONFIRMADA).setParameter("pending",BookingStatus.PENDIENTE).getResultList();
    }
    public List<SessionBooking> agenda(Long id, Instant start, Instant end) {
        return em.createQuery("select b from SessionBooking b where b.specialist.id=:id and b.orderItem is null and b.startsAt<:end and b.endsAt>:start order by b.startsAt,b.id",SessionBooking.class)
            .setParameter("id",id).setParameter("start",start).setParameter("end",end).getResultList();
    }
    public Long specialistForCustomerAppointment(Long userId, Long appointmentId) {
        return em.createQuery("""
                select b.specialist.id from SessionBooking b
                where b.id = :id and b.customer.user.id = :userId and b.orderItem is null
                """, Long.class)
                .setParameter("id", appointmentId)
                .setParameter("userId", userId)
                .getResultStream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada."));
    }

    public List<SessionBooking> forCustomer(Long userId) {
        return em.createQuery("""
                select b from SessionBooking b
                join fetch b.service
                join fetch b.specialist
                join fetch b.customer c
                left join fetch b.location
                where c.user.id = :userId and b.orderItem is null
                order by b.startsAt desc, b.id desc
                """, SessionBooking.class)
                .setParameter("userId", userId).getResultList();
    }

    public List<SessionBooking> pending(Long specialistId, Instant now) {
        return em.createQuery("""
                select b from SessionBooking b
                where b.specialist.id = :id and b.orderItem is null
                  and b.status = :pending and b.startsAt > :now
                  and (b.expiresAt is null or b.expiresAt > :now)
                order by b.startsAt, b.id
                """, SessionBooking.class)
                .setParameter("id", specialistId)
                .setParameter("pending", BookingStatus.PENDIENTE)
                .setParameter("now", now)
                .getResultList();
    }
}

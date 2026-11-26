package com.hean.consigueventas.oonabe.support.service;

import com.hean.consigueventas.oonabe.booking.entity.SessionBooking;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.support.dto.SupportDtos.*;
import com.hean.consigueventas.oonabe.support.entity.*;
import com.hean.consigueventas.oonabe.support.enums.*;
import com.hean.consigueventas.oonabe.support.mapper.SupportMapper;
import com.hean.consigueventas.oonabe.support.repository.*;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketService {
    private final SupportTicketRepository tickets;
    private final SupportResponseRepository responses;
    private final SupportAttachmentRepository attachments;
    private final SupportAuditRepository audits;
    private final SupportPaymentRepository payments;
    private final RefundRequestRepository refunds;
    private final EventBookingRepository bookings;
    private final EventRepository events;
    private final SupportAccess access;
    private final SupportMapper mapper;
    private final EntityManager em;

    public static ResponseStatusException bad(String text) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, text);
    }

    public static ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado");
    }

    public SupportTicket read(Long id) {
        var t = tickets.findById(id).orElseThrow(SupportTicketService::missing);
        if (!t.getUser().getId().equals(access.user().getId()) && !access.canReadAll())
            throw new org.springframework.security.access.AccessDeniedException("Ticket privado");
        return t;
    }

    public SupportTicket lock(Long id) {
        return tickets.lock(id).orElseThrow(SupportTicketService::missing);
    }

    public void audit(SupportTicket ticket, String action, String detail, boolean internal) {
        var a = new SupportAudit();
        a.setTicket(ticket);
        a.setActor(access.user());
        a.setAction(action);
        a.setDetail(detail);
        a.setInternal(internal);
        audits.save(a);
    }

    public SupportTicket createEntity(TicketInput in) {
        access.customer();
        var u = access.user();
        var t = new SupportTicket();
        t.setUser(u);
        t.setCategory(in.category());
        t.setSubject(in.subject().trim());
        t.setDescription(in.description().trim());
        t.setPriority(in.priority() == null ? TicketPriority.MEDIA : in.priority());
        if (in.bookingId() != null && in.sessionBookingId() != null)
            throw bad("Selecciona una sola reserva relacionada");
        if (in.bookingId() != null) {
            var b = bookings.findById(in.bookingId()).orElseThrow(SupportTicketService::missing);
            if (!b.getCustomer().getUser().getId().equals(u.getId())) throw bad("Reserva ajena");
            t.setBooking(b);
            t.setEvent(b.getOccurrence().getEvent());
        }
        if (in.sessionBookingId() != null) {
            var session = em.find(SessionBooking.class, in.sessionBookingId());
            if (session == null) throw missing();
            if (!session.getCustomer().getUser().getId().equals(u.getId()))
                throw new org.springframework.security.access.AccessDeniedException(
                        "Reserva de sesión ajena");
            if (in.eventId() != null) throw bad("Una sesión individual no puede vincularse a un evento");
            t.setSessionBooking(session);
        }
        if (in.eventId() != null) {
            if (t.getEvent() != null && !t.getEvent().getId().equals(in.eventId()))
                throw bad("El evento no corresponde a la reserva seleccionada");
            var owned =
                    bookings.findByCustomer_User_IdOrderByCreatedAtDesc(u.getId()).stream()
                            .anyMatch(booking -> booking.getOccurrence().getEvent().getId().equals(in.eventId()));
            if (!owned)
                throw new org.springframework.security.access.AccessDeniedException(
                        "Evento sin reserva propia");
            t.setEvent(events.findById(in.eventId()).orElseThrow(SupportTicketService::missing));
        }
        if (in.transactionId() != null) {
            var p = payments.findById(in.transactionId()).orElseThrow(SupportTicketService::missing);
            if (!p.getOrder().getCustomer().getUser().getId().equals(u.getId()))
                throw bad("Operación ajena");
            t.setPayment(p);
        }
        if (in.complaintReference() != null && !in.complaintReference().isBlank())
            t.setComplaintReference(in.complaintReference().trim());
        t.setTicketNumber("TMP-" + UUID.randomUUID().toString().substring(0, 20));
        tickets.saveAndFlush(t);
        t.setTicketNumber("OONA-" + Year.now().getValue() + "-" + String.format("%06d", t.getId()));
        audit(t, "CREADO", "Solicitud registrada", false);
        return t;
    }

    /**
     * Referencias exclusivamente del usuario autenticado; incluye reservas canceladas para reportar
     * incidencias.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> references() {
        access.customer();
        Long userId = access.user().getId();
        List<Map<String, Object>> result = new ArrayList<>();
        for (var booking : bookings.findByCustomer_User_IdOrderByCreatedAtDesc(userId)) {
            var item = new LinkedHashMap<String, Object>();
            item.put("key", "EVENT:" + booking.getId());
            item.put("kind", "EVENT");
            item.put("bookingId", booking.getId());
            item.put("sessionBookingId", null);
            item.put("eventId", booking.getOccurrence().getEvent().getId());
            item.put("title", booking.getOccurrence().getEvent().getTitle());
            item.put("code", booking.getCode());
            item.put("startsAt", booking.getOccurrence().getStartsAt());
            item.put("status", booking.getStatus());
            result.add(item);
        }
        for (var booking :
                em.createQuery(
                                "select b from SessionBooking b join fetch b.service where"
                                        + " b.customer.user.id=:userId order by b.createdAt desc",
                                SessionBooking.class)
                        .setParameter("userId", userId)
                        .getResultList()) {
            var item = new LinkedHashMap<String, Object>();
            item.put("key", "SESSION:" + booking.getId());
            item.put("kind", "SESSION");
            item.put("bookingId", null);
            item.put("sessionBookingId", booking.getId());
            item.put("eventId", null);
            item.put("title", booking.getService().getTitle());
            item.put("code", booking.getCode());
            item.put("startsAt", booking.getStartsAt());
            item.put("status", booking.getStatus());
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> create(TicketInput in) {
        if (in.category() == TicketCategory.REEMBOLSOS)
            throw bad("Utiliza el formulario Solicitar reembolso para vincular el pago y el importe");
        return mapper.ticket(createEntity(in));
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> my(int page) {
        access.customer();
        return tickets.findByUserId(access.user().getId(), page(page)).map(mapper::ticket);
    }

    public static Pageable page(int page) {
        return PageRequest.of(Math.max(0, page), 20, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> all(
            TicketCategory category,
            TicketStatus status,
            TicketPriority priority,
            boolean complaints,
            int page) {
        if (!access.canReadAll())
            throw new org.springframework.security.access.AccessDeniedException("Acceso restringido");
        Specification<SupportTicket> s = (r, q, b) -> b.conjunction();
        if (category != null) s = s.and((r, q, b) -> b.equal(r.get("category"), category));
        if (status != null) s = s.and((r, q, b) -> b.equal(r.get("status"), status));
        if (priority != null) s = s.and((r, q, b) -> b.equal(r.get("priority"), priority));
        if (complaints) s = s.and((r, q, b) -> b.isNotNull(r.get("complaintReference")));
        return tickets.findAll(s, page(page)).map(mapper::ticket);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(Long id) {
        var t = read(id);
        boolean staff = access.canReadAll();
        var m = mapper.ticket(t);
        m.put(
                "responses",
                responses.findByTicketIdOrderByCreatedAtAscIdAsc(id).stream()
                        .filter(r -> staff || !r.isInternal())
                        .map(mapper::response)
                        .toList());
        m.put(
                "attachments",
                attachments.findByTicketIdOrderByCreatedAtAsc(id).stream()
                        .filter(a -> staff || a.getResponse() == null || !a.getResponse().isInternal())
                        .map(mapper::attachment)
                        .toList());
        m.put(
                "history",
                audits.findByTicketIdOrderByCreatedAtAscIdAsc(id).stream()
                        .filter(a -> staff || !a.isInternal())
                        .map(mapper::audit)
                        .toList());
        m.put("refundRequestId", refunds.findByTicketId(id).map(RefundRequest::getId).orElse(null));
        return m;
    }

    void closeAfterRefundDecision(Long ticketId) {
        var ticket = lock(ticketId);
        if (ticket.getStatus() == TicketStatus.CERRADO) return;
        var previous = ticket.getStatus();
        ticket.setStatus(TicketStatus.CERRADO);
        ticket.setUpdatedAt(Instant.now());
        audit(ticket, "ESTADO", previous + " → CERRADO: decisión de reembolso registrada", false);
    }

    public void transition(SupportTicket t, TicketStatus to, String reason) {
        if (!TicketTransitions.allows(t.getStatus(), to))
            throw bad("Transición no permitida: " + t.getStatus() + " → " + to);
        var old = t.getStatus();
        t.setStatus(to);
        t.setUpdatedAt(Instant.now());
        audit(t, "ESTADO", old + " → " + to + ": " + reason, false);
    }

    public Map<String, Object> status(Long id, StatusInput in) {
        var t = lock(id);
        access.admin();
        if (in.status() == TicketStatus.REABIERTO)
            throw bad("La reapertura corresponde al solicitante");
        transition(t, in.status(), in.reason());
        return mapper.ticket(t);
    }

    public Map<String, Object> priority(Long id, PriorityInput in) {
        var t = lock(id);
        access.admin();
        var old = t.getPriority();
        t.setPriority(in.priority());
        t.setUpdatedAt(Instant.now());
        audit(t, "PRIORIDAD", old + " → " + in.priority() + ": " + in.reason(), false);
        return mapper.ticket(t);
    }

    public Map<String, Object> reopen(Long id, String reason) {
        access.customer();
        var t = lock(id);
        if (!t.getUser().getId().equals(access.user().getId()))
            throw new org.springframework.security.access.AccessDeniedException("Ticket ajeno");
        transition(t, TicketStatus.REABIERTO, reason);
        return mapper.ticket(t);
    }

    public Map<String, Object> respond(Long id, ResponseInput in, boolean staff) {
        var t = lock(id);
        if (staff) access.admin();
        else {
            access.customer();
            if (!t.getUser().getId().equals(access.user().getId()))
                throw new org.springframework.security.access.AccessDeniedException("Ticket ajeno");
            if (in.internal() || in.requestInformation()) throw bad("No se permiten notas internas");
        }
        if (t.getStatus() == TicketStatus.RESUELTO || t.getStatus() == TicketStatus.CERRADO)
            throw bad("Reabre el ticket para responder");
        if (in.requestInformation() && in.internal())
            throw bad("La solicitud de información debe ser pública");
        if (in.requestInformation() && t.getStatus() != TicketStatus.ESPERANDO_USUARIO)
            transition(t, TicketStatus.ESPERANDO_USUARIO, "Se solicita información adicional");
        if (!staff && t.getStatus() == TicketStatus.ESPERANDO_USUARIO)
            transition(t, TicketStatus.EN_REVISION, "Usuario añadió información");
        var r = new SupportResponse();
        r.setTicket(t);
        r.setSender(access.user());
        r.setMessage(in.message().trim());
        r.setInternal(in.internal());
        responses.save(r);
        t.setUpdatedAt(Instant.now());
        audit(
                t, in.internal() ? "NOTA_INTERNA" : "RESPUESTA", "Se añadió una respuesta", in.internal());
        return mapper.response(r);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        if (!access.canReadAll())
            throw new org.springframework.security.access.AccessDeniedException("Acceso restringido");
        var counts =
                em.createQuery(
                                "select t.status,count(t) from SupportTicket t group by t.status", Object[].class)
                        .getResultList();
        Map<String, Object> m = new LinkedHashMap<>();
        long total = 0;
        for (var s : TicketStatus.values()) m.put(s.name(), 0L);
        for (var r : counts) {
            m.put(r[0].toString(), r[1]);
            total += (Long) r[1];
        }
        m.put("total", total);
        m.put(
                "reclamaciones",
                em.createQuery(
                                "select count(t) from SupportTicket t where t.complaintReference is not null",
                                Long.class)
                        .getSingleResult());
        m.put(
                "reembolsos",
                em.createQuery("select count(r) from RefundRequest r", Long.class).getSingleResult());
        m.put(
                "criticos",
                em.createQuery(
                                "select count(t) from SupportTicket t where t.priority=:p and t.status not in (:s)",
                                Long.class)
                        .setParameter("p", TicketPriority.CRITICA)
                        .setParameter("s", List.of(TicketStatus.RESUELTO, TicketStatus.CERRADO))
                        .getSingleResult());
        return m;
    }
}

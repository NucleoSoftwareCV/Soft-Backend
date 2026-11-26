package com.hean.consigueventas.oonabe.support.service;

import com.hean.consigueventas.oonabe.notification.service.NotificationService;
import com.hean.consigueventas.oonabe.payment.domain.PaymentStatus;
import com.hean.consigueventas.oonabe.payment.entity.Payment;
import com.hean.consigueventas.oonabe.support.dto.SupportDtos.*;
import com.hean.consigueventas.oonabe.support.entity.*;
import com.hean.consigueventas.oonabe.support.enums.*;
import com.hean.consigueventas.oonabe.support.mapper.SupportMapper;
import com.hean.consigueventas.oonabe.support.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundRequestService {
    private final RefundRequestRepository refunds;
    private final NotificationService notifications;
    private final SupportResponseRepository responses;
    private final SupportPaymentRepository payments;
    private final SupportTicketService tickets;
    private final SupportAccess access;
    private final SupportMapper mapper;
    private final EntityManager em;

    private BigDecimal reserved(Payment p, Long excluded) {
        BigDecimal sum =
                em.createQuery(
                                "select coalesce(sum(r.amount),0) from Refund r where r.payment.id=:id and r.status"
                                        + " in (:statuses)",
                                BigDecimal.class)
                        .setParameter("id", p.getId())
                        .setParameter(
                                "statuses",
                                List.of(
                                        com.hean.consigueventas.oonabe.payment.domain.RefundStatus.PENDIENTE,
                                        com.hean.consigueventas.oonabe.payment.domain.RefundStatus.APROBADO))
                        .getSingleResult();
        for (var r : refunds.findByTransactionId(p.getId()))
            if (!Objects.equals(r.getId(), excluded) && r.getStatus() != RefundStatus.RECHAZADO)
                sum =
                        sum.add(r.getApprovedAmount() == null ? r.getRequestedAmount() : r.getApprovedAmount());
        return sum;
    }

    private void eligible(Payment p) {
        if (p.getStatus() != PaymentStatus.APROBADO)
            throw SupportTicketService.bad("Solo se evalúan pagos confirmados y no reembolsados");
    }

    public Map<String, Object> create(RefundInput in) {
        access.customer();
        var p = payments.lock(in.transactionId()).orElseThrow(SupportTicketService::missing);
        if (!p.getOrder().getCustomer().getUser().getId().equals(access.user().getId()))
            throw new org.springframework.security.access.AccessDeniedException("Operación ajena");
        eligible(p);
        if (in.requestedAmount().compareTo(p.getAmount().subtract(reserved(p, null))) > 0)
            throw SupportTicketService.bad(
                    "El importe supera el saldo elegible o hay otra solicitud pendiente");
        SupportTicket t;
        if (in.ticketId() == null) {
            t =
                    tickets.createEntity(
                            new TicketInput(
                                    TicketCategory.REEMBOLSOS,
                                    "Solicitud de reembolso",
                                    in.description(),
                                    TicketPriority.MEDIA,
                                    null,
                                    null,
                                    p.getId(),
                                    null,
                                    null));
        } else {
            t = tickets.lock(in.ticketId());
            if (!t.getUser().getId().equals(access.user().getId()))
                throw new org.springframework.security.access.AccessDeniedException("Ticket ajeno");
            if (t.getCategory() != TicketCategory.REEMBOLSOS)
                throw SupportTicketService.bad("El ticket debe corresponder a un reembolso");
            if (refunds.findByTicketId(t.getId()).isPresent())
                throw SupportTicketService.bad("El ticket ya tiene una solicitud de reembolso");
            if (t.getPayment() != null && !t.getPayment().getId().equals(p.getId()))
                throw SupportTicketService.bad("Selecciona el pago vinculado al ticket");
            t.setPayment(p);
            t.setUpdatedAt(Instant.now());
            var response = new SupportResponse();
            response.setTicket(t);
            response.setSender(access.user());
            response.setMessage(in.description().trim());
            response.setInternal(false);
            responses.save(response);
        }
        var r = new RefundRequest();
        r.setTicket(t);
        r.setTransaction(p);
        r.setReason(in.reason());
        r.setRequestedAmount(in.requestedAmount());
        refunds.save(r);
        tickets.audit(t, "REEMBOLSO_SOLICITADO", in.requestedAmount() + " " + p.getCurrency(), false);
        return mapper.refund(r);
    }

    public RefundRequest read(Long id) {
        var r = refunds.findById(id).orElseThrow(SupportTicketService::missing);
        tickets.read(r.getTicket().getId());
        return r;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(Long id) {
        return mapper.refund(read(id));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Map<String, Object>> my(int page) {
        access.customer();
        return refunds
                .findByTicketUserId(access.user().getId(), SupportTicketService.page(page))
                .map(mapper::refund);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Map<String, Object>> all(int page) {
        if (!access.canReadAll())
            throw new org.springframework.security.access.AccessDeniedException("Acceso restringido");
        return refunds.findAll(SupportTicketService.page(page)).map(mapper::refund);
    }

    private RefundRequest locked(Long id) {
        var initial = refunds.findById(id).orElseThrow(SupportTicketService::missing);
        payments.lock(initial.getTransaction().getId()).orElseThrow(SupportTicketService::missing);
        return refunds.lock(id).orElseThrow(SupportTicketService::missing);
    }

    public Map<String, Object> evaluate(Long id) {
        access.admin();
        var r = locked(id);
        if (r.getStatus() != RefundStatus.SOLICITADO)
            throw SupportTicketService.bad("Solicitud ya evaluada");
        r.setStatus(RefundStatus.EN_EVALUACION);
        tickets.audit(
                r.getTicket(), "REEMBOLSO_EN_EVALUACION", "Se inició la evaluación comercial", false);
        return mapper.refund(r);
    }

    public Map<String, Object> decide(Long id, DecisionInput in, boolean approve) {
        access.admin();
        var r = locked(id);
        if (r.getStatus() != RefundStatus.SOLICITADO && r.getStatus() != RefundStatus.EN_EVALUACION)
            throw SupportTicketService.bad("La solicitud ya tiene una decisión");
        if (approve) {
            eligible(r.getTransaction());
            if (in.approvedAmount() == null
                    || in.approvedAmount().compareTo(r.getRequestedAmount()) > 0
                    || in.approvedAmount()
                    .compareTo(
                            r.getTransaction()
                                    .getAmount()
                                    .subtract(reserved(r.getTransaction(), r.getId())))
                    > 0) throw SupportTicketService.bad("Importe autorizado inválido");
            r.setApprovedAmount(in.approvedAmount());
        }
        r.setAdminObservation(in.reason());
        r.setStatus(approve ? RefundStatus.APROBADO : RefundStatus.RECHAZADO);
        notifications.notifyRefundDecision(r.getTicket().getUser(), r.getId(),
                r.getTicket().getTicketNumber(), approve);
        tickets.audit(
                r.getTicket(),
                "REEMBOLSO_" + r.getStatus(),
                in.reason() + (approve ? ". Importe: " + in.approvedAmount() : ""),
                false);
        tickets.closeAfterRefundDecision(r.getTicket().getId());
        return mapper.refund(r);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> operations() {
        access.customer();
        return payments.findByOrderCustomerUserIdOrderByCreatedAtDesc(access.user().getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.APROBADO)
                .map(
                        p ->
                                Map.<String, Object>of(
                                        "id",
                                        p.getId(),
                                        "orderCode",
                                        p.getOrder().getCode(),
                                        "amount",
                                        p.getAmount(),
                                        "currency",
                                        p.getCurrency(),
                                        "eligibleAmount",
                                        p.getAmount().subtract(reserved(p, null)).max(BigDecimal.ZERO)))
                .toList();
    }
}

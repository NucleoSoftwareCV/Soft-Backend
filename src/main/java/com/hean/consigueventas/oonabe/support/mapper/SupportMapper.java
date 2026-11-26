package com.hean.consigueventas.oonabe.support.mapper;

import com.hean.consigueventas.oonabe.support.entity.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class SupportMapper {
    public Map<String, Object> ticket(SupportTicket t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("complaintReference", t.getComplaintReference());
        m.put("id", t.getId());
        m.put("ticketNumber", t.getTicketNumber());
        m.put("category", t.getCategory());
        m.put("subject", t.getSubject());
        m.put("description", t.getDescription());
        m.put("priority", t.getPriority());
        m.put("status", t.getStatus());
        m.put("createdAt", t.getCreatedAt());
        m.put("updatedAt", t.getUpdatedAt());
        m.put("userId", t.getUser().getId());
        m.put("userName", t.getUser().getUsername());
        m.put("userEmail", t.getUser().getEmail());
        m.put("assignedUserId", t.getAssignedAdmin() == null ? null : t.getAssignedAdmin().getId());
        m.put("assignedName", t.getAssignedAdmin() == null ? null : t.getAssignedAdmin().getUsername());
        m.put("bookingCode", t.getBooking() == null ? null : t.getBooking().getCode());
        m.put("sessionBookingId", t.getSessionBooking() == null ? null : t.getSessionBooking().getId());
        m.put(
                "sessionBookingCode",
                t.getSessionBooking() == null ? null : t.getSessionBooking().getCode());
        m.put(
                "sessionTitle",
                t.getSessionBooking() == null ? null : t.getSessionBooking().getService().getTitle());
        m.put("eventTitle", t.getEvent() == null ? null : t.getEvent().getTitle());
        m.put("bookingId", t.getBooking() == null ? null : t.getBooking().getId());
        m.put("eventId", t.getEvent() == null ? null : t.getEvent().getId());
        m.put("transactionId", t.getPayment() == null ? null : t.getPayment().getId());
        return m;
    }

    public Map<String, Object> response(SupportResponse r) {
        return Map.of(
                "id",
                r.getId(),
                "sender",
                r.getSender().getUsername(),
                "senderId",
                r.getSender().getId(),
                "message",
                r.getMessage(),
                "internal",
                r.isInternal(),
                "createdAt",
                r.getCreatedAt());
    }

    public Map<String, Object> attachment(SupportAttachment a) {
        return Map.of(
                "id",
                a.getId(),
                "fileName",
                a.getFileName(),
                "contentType",
                a.getContentType(),
                "createdAt",
                a.getCreatedAt());
    }

    public Map<String, Object> audit(SupportAudit a) {
        return Map.of(
                "id",
                a.getId(),
                "actor",
                a.getActor().getUsername(),
                "action",
                a.getAction(),
                "detail",
                a.getDetail(),
                "createdAt",
                a.getCreatedAt());
    }

    public Map<String, Object> refund(RefundRequest r) {
        var m = new LinkedHashMap<String, Object>();
        m.put("id", r.getId());
        m.put("ticketId", r.getTicket().getId());
        m.put("ticketNumber", r.getTicket().getTicketNumber());
        m.put("transactionId", r.getTransaction().getId());
        m.put("originalAmount", r.getTransaction().getAmount());
        m.put("currency", r.getTransaction().getCurrency());
        m.put("requestedAmount", r.getRequestedAmount());
        m.put("approvedAmount", r.getApprovedAmount());
        m.put("reason", r.getReason());
        m.put("observation", r.getAdminObservation());
        m.put("status", r.getStatus());
        m.put("providerRefundId", r.getProviderRefundId());
        m.put("processedAt", r.getProcessedAt());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }

}

package com.hean.consigueventas.oonabe.support.entity;

import com.hean.consigueventas.oonabe.payment.entity.Payment;
import com.hean.consigueventas.oonabe.support.enums.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "refund_requests")
@Getter
@Setter
public class RefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private SupportTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Payment transaction;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal requestedAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal approvedAmount;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(columnDefinition = "text")
    private String adminObservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus status = RefundStatus.SOLICITADO;

    @Column(length = 150, unique = true)
    private String providerRefundId;

    @Column(nullable = false, unique = true, length = 80)
    private String idempotencyKey = java.util.UUID.randomUUID().toString();

    private Instant processedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}

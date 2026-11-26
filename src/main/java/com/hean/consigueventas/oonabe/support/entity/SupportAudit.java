package com.hean.consigueventas.oonabe.support.entity;

import com.hean.consigueventas.oonabe.support.enums.*;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "support_audit")
@Getter
@Setter
public class SupportAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private SupportTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 2000)
    private String detail;

    @Column(nullable = false)
    private boolean internal;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}

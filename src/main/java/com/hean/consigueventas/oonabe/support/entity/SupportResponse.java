package com.hean.consigueventas.oonabe.support.entity;

import com.hean.consigueventas.oonabe.support.enums.*;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "support_responses")
@Getter
@Setter
public class SupportResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "is_internal", nullable = false)
    private boolean internal;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}

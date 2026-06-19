package com.hean.consigueventas.oonabe.audit.entity;

import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "action", nullable = false, length = 40)
    private String action;

    @Column(name = "entity_name", nullable = false, length = 80)
    private String entity;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "previous_data", columnDefinition = "TEXT")
    private String previousData;

    @Column(name = "new_data", columnDefinition = "TEXT")
    private String newData;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

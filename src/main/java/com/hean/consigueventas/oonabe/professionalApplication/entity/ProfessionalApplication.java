package com.hean.consigueventas.oonabe.professionalApplication.entity;

import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalType;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "role_promotion_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_professional_application_user",
                columnNames = "user_id"
        ),
        indexes = {
                @Index(
                        name = "idx_professional_application_status_created",
                        columnList = "status, created_at"
                ),
                @Index(
                        name = "idx_professional_application_email",
                        columnList = "email"
                )
        }
)
@Getter
@Setter
public class ProfessionalApplication extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "user_id",
            nullable = true,
            unique = true
    )
    private User user;

    @Column(
            name = "full_name",
            nullable = false,
            length = 200
    )
    private String fullName;

    @Column(
            name = "email",
            nullable = false,
            length = 150
    )
    private String email;

    @Column(
            name = "city",
            nullable = false,
            length = 100
    )
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "professional_type",
            length = 40
    )
    private ProfessionalType professionalType;

    @Column(
            name = "whatsapp_phone",
            length = 25
    )
    private String whatsappPhone;

    @Column(
            name = "reason",
            length = 500
    )
    private String motivation;

    @Column(name = "privacy_accepted_at")
    private Instant privacyAcceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private ProfessionalApplicationStatus status =
            ProfessionalApplicationStatus.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by")
    private User evaluatedBy;

    @Column(name = "evaluated_at")
    private Instant evaluatedAt;

    @Column(
            name = "rejection_reason",
            length = 500
    )
    private String rejectionReason;

    @Version
    @Column(
            name = "version",
            nullable = false,
            columnDefinition = "bigint default 0"
    )
    private long version;
}
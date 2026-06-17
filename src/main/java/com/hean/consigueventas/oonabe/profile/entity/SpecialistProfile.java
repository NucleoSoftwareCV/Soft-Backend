package com.hean.consigueventas.oonabe.profile.entity;

import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "perfil_profesionales")
@Getter
@Setter
public class SpecialistProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private User user;

    @Column(name = "slug", nullable = false, unique = true, length = 160)
    private String slug;

    @Column(name = "nombre_publico", nullable = false, length = 150)
    private String publicName;

    @Column(name = "biografia", nullable = false, columnDefinition = "TEXT")
    private String biography;

    @Column(name = "foto_url", nullable = false, columnDefinition = "TEXT")
    private String photoUrl;

    @Column(name = "telefono_whatsapp", nullable = false, length = 25)
    private String whatsappPhone;

    @Column(name = "email_publico", length = 150)
    private String publicEmail;

    @Column(name = "sitio_web", columnDefinition = "TEXT")
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aprobacion", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_publicacion", nullable = false, length = 20)
    private PublicationStatus publicationStatus = PublicationStatus.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private User approvedBy;

    @Column(name = "aprobado_at")
    private Instant approvedAt;

    @Column(name = "motivo_rechazo")
    private String rejectionReason;
}

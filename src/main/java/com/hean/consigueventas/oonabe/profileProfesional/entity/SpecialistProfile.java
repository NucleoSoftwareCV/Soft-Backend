package com.hean.consigueventas.oonabe.profileProfesional.entity;

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
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "specialist_profiles")
@Getter
@Setter
public class SpecialistProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "slug", nullable = false, unique = true, length = 160)
    private String slug;

    @Column(name = "public_name", nullable = false, length = 150)
    private String publicName;

    @Column(name = "profile_category", nullable = false, length = 30)
    private String profileCategory = "PROFESIONALES";

    @Size(max = 255)
    @Column(name = "biography", length = 255)
    private String biography;

    @Size(max = 5000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    // Medidas exactas de almacenamiento: 512x512.
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    // Medidas exactas de almacenamiento: 1248x256.
    @Column(name = "banner_url", columnDefinition = "TEXT")
    private String bannerUrl;

    @Column(name = "whatsapp_phone", nullable = false, length = 25)
    private String whatsappPhone;

    @Column(name = "public_email", length = 150)
    private String publicEmail;

    @Column(name = "website", columnDefinition = "TEXT")
    private String website;

    @Column(name = "phone_number", length = 25)
    private String phoneNumber;

    //Estado de aprobacion: PENDIENTE, APROBADO, RECHAZADO
    //SE INICIA EN PENDIENTE
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDIENTE;

    //Estado de publicacion: BORRADOR, PUBLICADO
    //SE INICIA EN BORRADOR
    @Enumerated(EnumType.STRING)
    @Column(name = "publication_status", nullable = false, length = 20)
    private PublicationStatus publicationStatus = PublicationStatus.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "show_upcoming_events", nullable = false, columnDefinition = "boolean not null default true")
    private boolean showUpcomingEvents = true;

    @Column(name = "show_one_to_one_sessions", nullable = false, columnDefinition = "boolean not null default true")
    private boolean showOneToOneSessions = true;

    @Column(name = "show_gallery", nullable = false, columnDefinition = "boolean not null default true")
    private boolean showGallery = true;
}

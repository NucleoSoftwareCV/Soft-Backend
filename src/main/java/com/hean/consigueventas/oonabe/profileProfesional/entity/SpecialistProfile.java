package com.hean.consigueventas.oonabe.profileProfesional.entity;

import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.profileType.entity.ProfileType;
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

    @Column(name = "biography", nullable = false, columnDefinition = "TEXT")
    private String biography;

    @Column(name = "photo_url", nullable = false, columnDefinition = "TEXT")
    private String photoUrl;

    @Column(name = "whatsapp_phone", nullable = false, length = 25)
    private String whatsappPhone;

    @Column(name = "public_email", length = 150)
    private String publicEmail;

    @Column(name = "website", columnDefinition = "TEXT")
    private String website;

    //Llamamos a ProfileType
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_perfil", nullable = false, length = 20)
    private ProfileType profileType = ProfileType.PROFESIONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDIENTE;

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
}
//Cuenta de profesionales
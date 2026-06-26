package com.hean.consigueventas.oonabe.oneToOneSession.entity;

import com.hean.consigueventas.oonabe.common.entity.AuditableEntity;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "one_to_one_services")
@Getter
@Setter
public class OneToOneService extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private SpecialistProfile specialist;

    @Column(name = "slug", nullable = false, unique = true, length = 180)
    private String slug;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "modality", nullable = false, length = 15)
    private SessionModality modality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PublicationStatus status = PublicationStatus.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @PrePersist
    public void generateSlug() {
        if (slug == null || slug.isBlank()) {
            slug = title == null ? null : title.toLowerCase()
                    .replace("ñ", "n")
                    .replace("á", "a")
                    .replace("é", "e")
                    .replace("í", "i")
                    .replace("ó", "o")
                    .replace("ú", "u")
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("(^-|-$)", "");
        }
    }
}

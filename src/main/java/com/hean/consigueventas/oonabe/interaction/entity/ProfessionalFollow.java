package com.hean.consigueventas.oonabe.interaction.entity;

import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "professional_follows",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_professional_follow_user_specialist",
                columnNames = {"user_id", "specialist_profile_id"}
        ),
        indexes = {
                @Index(name = "idx_professional_follow_user_date", columnList = "user_id, followed_at"),
                @Index(name = "idx_professional_follow_specialist", columnList = "specialist_profile_id")
        }
)
@Getter
@Setter
public class ProfessionalFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialist_profile_id", nullable = false)
    private SpecialistProfile specialistProfile;

    @Column(name = "followed_at", nullable = false, updatable = false)
    private Instant followedAt;

    @PrePersist
    void prePersist() {
        if (followedAt == null) {
            followedAt = Instant.now();
        }
    }
}

package com.hean.consigueventas.oonabe.profileProfesional.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "professional_languages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_professional_language_profile_language",
                        columnNames = {"specialist_profile_id", "language_name"}
                )
        }
)
@Getter
@Setter
public class ProfessionalLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialist_profile_id", nullable = false)
    private SpecialistProfile specialistProfile;

    @NotBlank
    @Size(max = 80)
    @Column(name = "language_name", nullable = false, length = 80)
    private String languageName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
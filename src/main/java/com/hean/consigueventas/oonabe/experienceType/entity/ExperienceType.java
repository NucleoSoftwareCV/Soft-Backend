package com.hean.consigueventas.oonabe.experienceType.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.text.Normalizer;
import java.util.Locale;

@Entity
@Table(name = "experience_types")
@Getter
@Setter
public class ExperienceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    @PreUpdate
    void normalizeSlug() {
        if (name != null) {
            slug = Normalizer.normalize(name, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("(^-|-$)", "");
        }
    }
}

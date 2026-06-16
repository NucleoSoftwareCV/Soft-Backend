package com.hean.consigueventas.oonabe.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categorias_evento")
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String description;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "activo", nullable = false)
    private boolean active = true;

    @PrePersist
    void prePersist() {
        if (slug == null || slug.isBlank()) {
            slug = name == null ? null : name.toLowerCase()
                    .replace("ñ", "n")
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("(^-|-$)", "");
        }
    }
}

package com.hean.consigueventas.oonabe.content.entity;

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

import java.time.Instant;

@Entity
@Table(name = "secciones_inicio")
@Getter
@Setter
public class HomeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave", nullable = false, unique = true, length = 80)
    private String key;

    @Column(name = "titulo", length = 180)
    private String title;

    @Column(name = "subtitulo", columnDefinition = "TEXT")
    private String subtitle;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "boton_texto", length = 80)
    private String buttonText;

    @Column(name = "boton_url", columnDefinition = "TEXT")
    private String buttonUrl;

    @Column(name = "orden", nullable = false)
    private Short sortOrder = 0;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}

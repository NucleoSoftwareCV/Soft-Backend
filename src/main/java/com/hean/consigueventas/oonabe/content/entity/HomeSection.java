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
@Table(name = "home_sections")
@Getter
@Setter
public class HomeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "section_key", nullable = false, unique = true, length = 80)
    private String key;

    @Column(name = "title", length = 180)
    private String title;

    @Column(name = "subtitle", columnDefinition = "TEXT")
    private String subtitle;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "button_text", length = 80)
    private String buttonText;

    @Column(name = "button_url", columnDefinition = "TEXT")
    private String buttonUrl;

    @Column(name = "sort_order", nullable = false)
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

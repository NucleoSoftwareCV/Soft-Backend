package com.hean.consigueventas.oonabe.profileProfesional.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "professional_images")
@Getter
@Setter
public class ProfessionalImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //Antes: specialist_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_profile_id", nullable = false)
    private SpecialistProfile specialistProfile;

    //Antes: url
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    //Antes: cover
    //true: es el banner
    //false: es una imagen de la galeria
    @Column(name = "is_banner", nullable = false)
    private boolean banner = false;

    //Antes: size_bytes
    @Column(name = "file_size_bytes")
    private Integer fileSizeBytes;

    // Antes: sort_order
    @Column(name = "display_order", nullable = false)
    private Short displayOrder = 0;
}
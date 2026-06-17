package com.hean.consigueventas.oonabe.profile.entity;

import com.hean.consigueventas.oonabe.common.enums.ImageFormat;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "imagenes_profesional")
@Getter
@Setter
public class ProfessionalImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private SpecialistProfile specialist;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "texto_alternativo", length = 180)
    private String alternativeText;

    @Column(name = "es_portada", nullable = false)
    private boolean cover;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato", length = 10)
    private ImageFormat format;

    @Column(name = "peso_bytes")
    private Integer sizeBytes;

    @Column(name = "orden", nullable = false)
    private Short sortOrder = 0;
}

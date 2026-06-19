package com.hean.consigueventas.oonabe.profileProfesional.entity;

import com.hean.consigueventas.oonabe.common.enums.SocialLinkType;
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
@Table(name = "professional_social_links")
@Getter
@Setter
public class ProfessionalSocialLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private SpecialistProfile specialist;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private SocialLinkType type;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "sort_order", nullable = false)
    private Short sortOrder = 0;
}
//Redes sociales de los profesionales

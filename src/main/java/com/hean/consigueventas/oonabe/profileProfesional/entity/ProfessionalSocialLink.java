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
@Table(name = "professional_social_links")
@Getter
@Setter
public class ProfessionalSocialLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Antes: specialist_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_profile_id", nullable = false)
    private SpecialistProfile specialistProfile;

    //Antes: type
    //Valores: INSTAGRAM, FACEBOOK, TIKTOK, YOUTUBE
    @Column(name = "platform", nullable = false, length = 30)
    private String platform;

    //Antes: url
    @Column(name = "profile_url", nullable = false, columnDefinition = "TEXT")
    private String profileUrl;

}
package com.hean.consigueventas.oonabe.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class ProfessionalSpecialtyId implements Serializable {
    @Column(name = "profesional_id")
    private Long specialistId;

    @Column(name = "especialidad_id")
    private Long specialtyId;
}

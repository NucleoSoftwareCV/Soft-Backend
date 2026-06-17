package com.hean.consigueventas.oonabe.profile.entity;

import com.hean.consigueventas.oonabe.catalog.entity.Specialty;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profesional_especialidades")
@Getter
@Setter
public class ProfessionalSpecialty {

    @EmbeddedId
    private ProfessionalSpecialtyId id = new ProfessionalSpecialtyId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("specialistId")
    @JoinColumn(name = "profesional_id")
    private SpecialistProfile specialist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("specialtyId")
    @JoinColumn(name = "especialidad_id")
    private Specialty specialty;
}

package com.hean.consigueventas.oonabe.profileProfesional.entity;

import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        //En tecnicas_profesional no pueden existir dos filas con el mismo profesional_id y el mismo tecnica_id
        name = "tecnicas_profesional",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_profesional_tecnica",
                        columnNames = {"profesional_id", "tecnica_id"}
                )
        }
)
@Getter
@Setter
public class ProfessionalTechnique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false)
    private SpecialistProfile specialistProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnica_id", nullable = false)
    private Technique technique;
}
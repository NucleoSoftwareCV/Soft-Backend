package com.hean.consigueventas.oonabe.profileProfesional.entity;

import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
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
        name = "professional_work_topics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_professional_work_topic",
                        columnNames = {
                                "specialist_profile_id",
                                "work_topic_id"
                        }
                )
        }
)
@Getter
@Setter
public class ProfessionalWorkTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Antes: specialist_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialist_profile_id", nullable = false)
    private SpecialistProfile specialistProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_topic_id", nullable = false)
    private WorkTopic workTopic;
}
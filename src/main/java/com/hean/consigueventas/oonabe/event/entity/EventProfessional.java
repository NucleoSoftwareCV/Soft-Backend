package com.hean.consigueventas.oonabe.event.entity;

import com.hean.consigueventas.oonabe.common.enums.EventProfessionalRole;
import com.hean.consigueventas.oonabe.profile.entity.SpecialistProfile;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_professionals")
@Getter
@Setter
public class EventProfessional {

    @EmbeddedId
    private EventProfessionalId id = new EventProfessionalId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("specialistId")
    @JoinColumn(name = "specialist_id")
    private SpecialistProfile specialist;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private EventProfessionalRole role;

    @Column(name = "principal", nullable = false)
    private boolean principal;
}

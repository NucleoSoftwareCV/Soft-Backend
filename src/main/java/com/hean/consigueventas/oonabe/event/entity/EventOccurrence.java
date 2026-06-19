package com.hean.consigueventas.oonabe.event.entity;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
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
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ocurrencias_evento")
@Getter
@Setter
public class EventOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Event event;

    @Column(name = "inicio_at", nullable = false)
    private Instant startsAt;

    @Column(name = "fin_at", nullable = false)
    private Instant endsAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Location location;

    @Column(name = "capacidad", nullable = false)
    @Min(0)
    private Integer capacity;

    @Column(name = "cupos_reservados", nullable = false)
    @Min(0)
    private Integer reservedSpots = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EventOccurrenceStatus status = EventOccurrenceStatus.PROGRAMADA;

    @Column(name = "url_virtual", columnDefinition = "TEXT")
    private String virtualUrl;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}

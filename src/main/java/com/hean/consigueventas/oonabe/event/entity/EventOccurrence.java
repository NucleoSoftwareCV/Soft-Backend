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
@Table(name = "event_occurrences")
@Getter
@Setter
public class EventOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "capacity", nullable = false)
    @Min(0)
    private Integer capacity;

    @Column(name = "reserved_spots", nullable = false)
    @Min(0)
    private Integer reservedSpots = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventOccurrenceStatus status = EventOccurrenceStatus.PROGRAMADA;

    @Column(name = "virtual_url", columnDefinition = "TEXT")
    private String virtualUrl;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}

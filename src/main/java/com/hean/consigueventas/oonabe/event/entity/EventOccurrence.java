package com.hean.consigueventas.oonabe.event.entity;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_link_id")
    private MeetingLink meetingLink;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "capacity", nullable = false)
    @Min(0)
    private Integer capacity;

    @Column(name = "reserved_spots", nullable = false)
    @Min(0)
    private Integer reservedSpots = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventOccurrenceStatus status = EventOccurrenceStatus.PROGRAMADA;

//    @Version
//    @Column(name = "version", nullable = false)
//    private Long version;
}

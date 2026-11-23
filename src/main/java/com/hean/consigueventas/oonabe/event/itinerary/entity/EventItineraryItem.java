package com.hean.consigueventas.oonabe.event.itinerary.entity;

import com.hean.consigueventas.oonabe.event.entity.Event;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Entity
@Table(name = "event_itinerary_items")
@Getter
@Setter
public class EventItineraryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}

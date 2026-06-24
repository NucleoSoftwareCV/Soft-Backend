package com.hean.consigueventas.oonabe.event.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "meeting_links")
@Getter
@Setter
public class MeetingLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_occurrence_id", nullable = false)
    private EventOccurrence eventOccurrence;

    @Column(name = "platform", nullable = false, length = 50)
    private String platform;


    @Column(name = "meeting_url", nullable = false, columnDefinition = "TEXT")
    private String meetingUrl;


    @Column(name = "meeting_id", length = 100)
    private String meetingId;


    @Column(name = "password", length = 100)
    private String password;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

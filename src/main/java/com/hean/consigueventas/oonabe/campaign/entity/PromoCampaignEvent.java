package com.hean.consigueventas.oonabe.campaign.entity;

import com.hean.consigueventas.oonabe.event.entity.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Experiencia incluida en una campaña. Se marca el evento completo, no ocurrencias sueltas. */
@Entity
@Table(name = "promo_campaign_events",
        uniqueConstraints = @UniqueConstraint(name = "uk_promo_campaign_event", columnNames = {"campaign_id", "event_id"}))
@Getter
@Setter
@NoArgsConstructor
public class PromoCampaignEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private PromoCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    public PromoCampaignEvent(PromoCampaign campaign, Event event) {
        this.campaign = campaign;
        this.event = event;
    }

    @PrePersist
    void prePersist() {
        if (addedAt == null) {
            addedAt = Instant.now();
        }
    }
}

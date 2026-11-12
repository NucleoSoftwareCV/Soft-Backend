package com.hean.consigueventas.oonabe.campaign.entity;

import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignEntryStatus;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Participación generada por una reserva confirmada. Una fila por reserva y campaña;
 * {@code ticketCount} refleja las plazas reservadas (una participación por plaza).
 */
@Entity
@Table(name = "promo_campaign_entries",
        uniqueConstraints = @UniqueConstraint(name = "uk_promo_campaign_entry_booking", columnNames = {"campaign_id", "event_booking_id"}))
@Getter
@Setter
public class PromoCampaignEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private PromoCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_booking_id", nullable = false)
    private EventBooking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @Column(name = "ticket_count", nullable = false)
    private int ticketCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampaignEntryStatus status = CampaignEntryStatus.VALIDA;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "void_reason", length = 300)
    private String voidReason;
}

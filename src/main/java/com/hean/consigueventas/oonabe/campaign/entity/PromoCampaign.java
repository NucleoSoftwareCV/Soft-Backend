package com.hean.consigueventas.oonabe.campaign.entity;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignPhase;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Campaña promocional (p. ej. un sorteo). El premio es solo informativo:
 * la plataforma no gestiona crédito ni saldo.
 */
@Entity
@Table(name = "promo_campaigns")
@Getter
@Setter
public class PromoCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    private long version;

    @Column(name = "slug", nullable = false, unique = true, length = 80)
    private String slug;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "subtitle", length = 300)
    private String subtitle;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "badge_label", nullable = false, length = 60)
    private String badgeLabel;

    @Column(name = "hero_image_url", length = 500)
    private String heroImageUrl;

    @Column(name = "prize_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal prizeAmount;

    @Column(name = "prize_currency", nullable = false, length = 3)
    private String prizeCurrency = "EUR";

    @Column(name = "prize_description", columnDefinition = "TEXT")
    private String prizeDescription;

    @Column(name = "participation_starts_at", nullable = false)
    private Instant participationStartsAt;

    @Column(name = "participation_ends_at", nullable = false)
    private Instant participationEndsAt;

    @Column(name = "draw_at")
    private Instant drawAt;

    @Column(name = "prize_activation_deadline")
    private Instant prizeActivationDeadline;

    @Column(name = "participation_conditions", columnDefinition = "TEXT")
    private String participationConditions;

    @Column(name = "legal_terms", columnDefinition = "TEXT")
    private String legalTerms;

    /** Tope de participaciones (plazas) por cliente; null = sin límite. */
    @Column(name = "max_entries_per_customer")
    private Integer maxEntriesPerCustomer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampaignStatus status = CampaignStatus.BORRADOR;

    @ElementCollection
    @CollectionTable(name = "promo_campaign_faqs", joinColumns = @JoinColumn(name = "campaign_id"))
    @OrderColumn(name = "item_order")
    private List<CampaignFaq> faqs = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CampaignPhase phaseAt(Instant now) {
        if (now.isBefore(participationStartsAt)) {
            return CampaignPhase.PROXIMA;
        }
        return now.isAfter(participationEndsAt) ? CampaignPhase.CERRADA : CampaignPhase.ABIERTA;
    }

    public boolean acceptsEntriesAt(Instant instant) {
        return status == CampaignStatus.PUBLICADA && phaseAt(instant) == CampaignPhase.ABIERTA;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}

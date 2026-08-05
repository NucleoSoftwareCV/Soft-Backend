package com.hean.consigueventas.oonabe.event.entity;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.enums.EventType;
import com.hean.consigueventas.oonabe.common.enums.EventPaymentMethod;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    private long version;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "summary", length = 300)
    private String summary;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "event_includes", joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "item_order")
    @Column(name = "item", nullable = false, length = 180)
    private List<String> includes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "event_highlights", joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "item_order")
    @Column(name = "highlight", nullable = false, length = 180)
    private List<String> highlights = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "event_what_to_bring", joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "item_order")
    @Column(name = "item", nullable = false, length = 180)
    private List<String> whatToBring = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "modality", nullable = false, length = 15)
    private EventModality modality;

    @Column(name = "starting_price", precision = 10, scale = 2)
    private BigDecimal priceFrom;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";
    @Column(name = "minimum_age")
    private Short minimumAge;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status = EventStatus.BORRADOR;

    @Column(name = "featured", nullable = false)
    private boolean featured;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 25)
    private EventType eventType;

    @Column(name = "is_recurring", nullable = false)
    private boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = true, length = 20)
    private EventPaymentMethod paymentMethod = EventPaymentMethod.WHATSAPP;

    public EventPaymentMethod getPaymentMethod() {
        return paymentMethod == null ? EventPaymentMethod.WHATSAPP : paymentMethod;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private SpecialistProfile specialist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

//    @Column(name = "approved_at")
//    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "event")
    private List<EventOccurrence> occurrences;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    @PostLoad
    void postLoad() {
        if (paymentMethod == null) {
            paymentMethod = EventPaymentMethod.WHATSAPP;
        }
    }
}

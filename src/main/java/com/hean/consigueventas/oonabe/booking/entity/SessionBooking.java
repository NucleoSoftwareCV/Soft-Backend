package com.hean.consigueventas.oonabe.booking.entity;

import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.enums.SessionModality;
import com.hean.consigueventas.oonabe.location.entity.Location;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrderItem;
import com.hean.consigueventas.oonabe.profile.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.profile.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.service.entity.OneToOneService;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reservas_sesion")
@Getter
@Setter
public class SessionBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_item_id")
    private PurchaseOrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private OneToOneService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private SpecialistProfile specialist;

    @Column(name = "inicio_at", nullable = false)
    private Instant startsAt;

    @Column(name = "fin_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false, length = 15)
    private SessionModality modality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Location location;

    @Column(name = "url_virtual", columnDefinition = "TEXT")
    private String virtualUrl;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "moneda", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDIENTE;

    @Column(name = "expira_at")
    private Instant expiresAt;

    @Column(name = "notas_cliente", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "cancelada_at")
    private Instant cancelledAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

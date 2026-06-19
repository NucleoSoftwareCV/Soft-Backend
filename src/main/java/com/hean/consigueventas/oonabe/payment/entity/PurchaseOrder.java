package com.hean.consigueventas.oonabe.payment.entity;

import com.hean.consigueventas.oonabe.common.enums.PurchaseOrderStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ordenes_compra")
@Getter
@Setter
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private CustomerProfile customer;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "importe_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "moneda", nullable = false, length = 3)
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private PurchaseOrderStatus status = PurchaseOrderStatus.ABIERTA;

    @Column(name = "expira_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "pagada_at")
    private Instant paidAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

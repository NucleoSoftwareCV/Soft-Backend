package com.hean.consigueventas.oonabe.payment.entity;

import com.hean.consigueventas.oonabe.common.enums.DigitalReceiptStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "recibos_digitales")
@Getter
@Setter
public class DigitalReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "numero", nullable = false, unique = true, length = 40)
    private String number;

    @Column(name = "cliente_nombre", nullable = false, length = 180)
    private String customerName;

    @Column(name = "cliente_email", nullable = false, length = 150)
    private String customerEmail;

    @Column(name = "descripcion_compra", nullable = false, columnDefinition = "TEXT")
    private String purchaseDescription;

    @Column(name = "importe", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "moneda", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private DigitalReceiptStatus status = DigitalReceiptStatus.VALIDO;

    @Column(name = "emitido_at", nullable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "anulado_at")
    private Instant annulledAt;
}

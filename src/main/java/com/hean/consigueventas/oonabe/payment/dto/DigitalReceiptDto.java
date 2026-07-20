package com.hean.consigueventas.oonabe.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class DigitalReceiptDto {
    private String number;
    private String customerName;
    private String customerEmail;
    private String purchaseDescription;
    private BigDecimal amount;
    private String currency;
    private Instant issuedAt;
}

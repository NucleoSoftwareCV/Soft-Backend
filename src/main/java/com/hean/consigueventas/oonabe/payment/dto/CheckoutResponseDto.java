package com.hean.consigueventas.oonabe.payment.dto;

import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CheckoutResponseDto {
    private String code;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalAmount;
    private String currency;
    private PurchaseOrderStatus status;
    private List<CheckoutResponseItemDto> items;
}

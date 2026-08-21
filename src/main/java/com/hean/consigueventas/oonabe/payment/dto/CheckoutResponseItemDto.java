package com.hean.consigueventas.oonabe.payment.dto;

import com.hean.consigueventas.oonabe.common.enums.PurchaseItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseItemDto {
    private Long referenceId;
    private PurchaseItemType itemType;
    private String description;
    private Short quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}

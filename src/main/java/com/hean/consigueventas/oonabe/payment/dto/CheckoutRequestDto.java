package com.hean.consigueventas.oonabe.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequestDto {

    @NotEmpty(message = "Debe haber al menos un ítem en la orden de compra")
    @Valid
    private List<CheckoutItemDto> items;

    private String billingName;
    private String billingAddress;
}

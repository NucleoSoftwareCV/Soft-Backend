package com.hean.consigueventas.oonabe.payment.dto;

import com.hean.consigueventas.oonabe.common.enums.PurchaseItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutItemDto {

    @NotNull(message = "El ID de referencia es obligatorio")
    private Long referenceId;

    @NotNull(message = "El tipo de ítem es obligatorio")
    private PurchaseItemType itemType;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Short quantity;
}

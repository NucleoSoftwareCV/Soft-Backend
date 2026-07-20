package com.hean.consigueventas.oonabe.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentProcessRequestDto {

    @NotBlank(message = "El método de pago es obligatorio (YAPE, TARJETA o WHATSAPP)")
    private String method;

    // Campos simulados para YAPE
    private String yapePhoneNumber;
    private String yapeOtp;

    // Campos simulados para TARJETA
    private String cardNumber;
    private String cardHolder;
    private String cardExpiration;
    private String cardCvv;
}

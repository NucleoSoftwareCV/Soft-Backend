package com.hean.consigueventas.oonabe.payment.controller;

import com.hean.consigueventas.oonabe.common.config.OpenApiConfig;
import com.hean.consigueventas.oonabe.common.security.SecurityUtils;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.payment.dto.*;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import com.hean.consigueventas.oonabe.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments/checkout")
@Tag(name = "Payments & Checkout", description = "Endpoints para checkout, simulación de pasarela de pagos y comprobantes digitales.")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear orden de compra (Checkout)", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "201", description = "Orden de compra creada exitosamente")
    public CheckoutResponseDto createOrder(@Valid @RequestBody CheckoutRequestDto request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        if (userId == null) {
            throw new BusinessLogicException("Debe estar autenticado para realizar el checkout");
        }
        return paymentService.createOrder(userId, request);
    }

    @PostMapping("/orders/{code}/pay")
    @Operation(summary = "Procesar pago simulado de una orden", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Pago procesado exitosamente")
    public PaymentResponseDto processPayment(
            @PathVariable String code,
            @Valid @RequestBody PaymentProcessRequestDto request) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        if (userId == null) {
            throw new BusinessLogicException("Debe estar autenticado para realizar el pago");
        }

        PurchaseOrder order = purchaseOrderRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la orden con código: " + code));

        // Validar que la orden pertenece al usuario autenticado
        if (!order.getCustomer().getUser().getId().equals(userId)) {
            throw new BusinessLogicException("La orden de compra no pertenece a tu usuario");
        }

        return paymentService.processPayment(code, request);
    }

    @GetMapping("/orders/{code}/whatsapp")
    @Operation(summary = "Generar link de redirección a WhatsApp para coordinar pago", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Link de WhatsApp generado")
    public WhatsAppRedirectDto getWhatsAppLink(@PathVariable String code) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        if (userId == null) {
            throw new BusinessLogicException("Debe estar autenticado para coordinar pago por WhatsApp");
        }

        PurchaseOrder order = purchaseOrderRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la orden con código: " + code));

        // Validar que la orden pertenece al usuario autenticado
        if (!order.getCustomer().getUser().getId().equals(userId)) {
            throw new BusinessLogicException("La orden de compra no pertenece a tu usuario");
        }

        return paymentService.generateWhatsAppLink(code);
    }

    @GetMapping("/receipts/{orderCode}")
    @Operation(summary = "Recuperar comprobante de pago digital por código de orden", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Comprobante digital recuperado exitosamente")
    public DigitalReceiptDto getDigitalReceipt(@PathVariable String orderCode) {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        if (userId == null) {
            throw new BusinessLogicException("Debe estar autenticado para ver su comprobante");
        }

        PurchaseOrder order = purchaseOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la orden con código: " + orderCode));

        // Validar que la orden pertenece al usuario autenticado
        if (!order.getCustomer().getUser().getId().equals(userId)) {
            throw new BusinessLogicException("El comprobante solicitado no pertenece a tu usuario");
        }

        return paymentService.getDigitalReceipt(orderCode);
    }

    @GetMapping("/bookings/me")
    @Operation(summary = "Listar mis reservas de eventos", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Reservas del usuario autenticado")
    public List<MyBookingResponseDto> getMyBookings() {
        Long userId = SecurityUtils.getAuthenticatedUserId();
        if (userId == null) {
            throw new BusinessLogicException("Debe estar autenticado para ver sus reservas");
        }
        return paymentService.getMyBookings(userId);
    }
}

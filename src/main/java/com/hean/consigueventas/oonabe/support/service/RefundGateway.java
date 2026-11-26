package com.hean.consigueventas.oonabe.support.service;

import java.math.BigDecimal;

/**
 * Implementar solo con el mecanismo autorizado de pagos. La misma clave debe ser idempotente en el
 * proveedor.
 */
public interface RefundGateway {
    record Result(String providerRefundId, boolean confirmed, boolean failed, String observation) {}

    boolean supports(String provider);

    Result submit(
            Long paymentId,
            String externalPaymentReference,
            BigDecimal amount,
            String currency,
            String idempotencyKey);

    /** Consulta al proveedor; nunca se acepta una confirmación del navegador. */
    Result lookup(String idempotencyKey, String providerRefundId);
}

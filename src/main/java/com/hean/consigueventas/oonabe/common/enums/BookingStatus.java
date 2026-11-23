package com.hean.consigueventas.oonabe.common.enums;

public enum BookingStatus {
    PENDIENTE,
    /** Aceptada por el profesional y pendiente de pago: aún no es una reserva confirmada. */
    ACEPTADA,
    REPROGRAMACION_PROPUESTA,
    CONFIRMADA,
    CANCELADA,
    LISTA_ESPERA,
    COMPLETADA,
    NO_ASISTIO
}

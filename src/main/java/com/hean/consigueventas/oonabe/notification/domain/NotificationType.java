package com.hean.consigueventas.oonabe.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Avisos automáticos por cambios de estado de una reserva, con su texto y su acción. */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    SOLICITUD_ENVIADA("Solicitud enviada correctamente",
            "Tu solicitud de reserva ha sido enviada. Estamos esperando la respuesta del profesional.", "Ver mi reserva"),
    RESERVA_ACEPTADA("¡Tu reserva fue aceptada!",
            "El profesional ha aceptado tu solicitud. Ya puedes realizar el pago para confirmar tu reserva.", "Realizar pago"),
    RESERVA_RECHAZADA("Tu solicitud de reserva fue rechazada",
            "El profesional no ha podido aceptar tu solicitud. Puedes consultar otras opciones disponibles.", "Ver mi reserva"),
    SOLICITUD_CANCELADA("Solicitud de reserva cancelada",
            "Tu solicitud ha sido cancelada correctamente.", "Ver mi reserva"),
    RESERVA_CONFIRMADA("¡Tu reserva está confirmada!",
            "Hemos verificado tu pago. Tu reserva se encuentra confirmada.", "Ver mi reserva"),
    RESERVA_GRATUITA_CONFIRMADA("¡Tu reserva está confirmada!",
            "El profesional ha aceptado tu solicitud. Tu reserva se encuentra confirmada.", "Ver mi reserva"),
    RESERVA_VENCIDA("Tu reserva ha vencido",
            "El plazo de tu reserva terminó sin completarse y la plaza se ha liberado.", "Ver mi reserva"),

    NUEVA_SOLICITUD("Nueva solicitud de reserva",
            "Has recibido una nueva solicitud. Revísala para aceptarla o rechazarla.", "Ver solicitud"),
    CANCELACION_CLIENTE("Un cliente canceló su reserva",
            "El cliente canceló la reserva antes de pagar y la plaza se ha liberado.", "Ver reserva"),
    PAGO_COMPLETADO("Pago completado",
            "El cliente completó el pago y la reserva está confirmada.", "Ver reserva"),
    RESERVA_EXPIRADA("Una reserva ha vencido",
            "La reserva venció sin completarse y la plaza se ha liberado.", "Ver reserva");

    private final String title;
    private final String message;
    private final String actionLabel;
}

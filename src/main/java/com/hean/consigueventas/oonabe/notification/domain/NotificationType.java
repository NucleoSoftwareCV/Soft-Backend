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
            "La reserva venció sin completarse y la plaza se ha liberado.", "Ver reserva"),

    CITA_SOLICITADA("Solicitud de cita enviada",
            "Tu solicitud de cita fue enviada. El profesional debe revisarla.", "Ver mi cita"),
    NUEVA_CITA_SOLICITADA("Nueva solicitud de cita",
            "Has recibido una solicitud de cita. Revísala desde tu agenda.", "Ver solicitud"),
    CITA_CONFIRMADA("¡Tu cita fue confirmada!",
            "El profesional confirmó tu cita. Revisa los detalles de la sesión.", "Ver mi cita"),
    CITA_ACEPTADA("¡Tu cita fue aceptada!",
            "El profesional aceptó tu cita. Realiza el pago para confirmarla.", "Realizar pago"),
    CITA_PAGO_CONFIRMADO("¡Tu cita está confirmada!",
            "Hemos verificado tu pago. Tu cita se encuentra confirmada.", "Ver mi cita"),
    CITA_PAGO_COMPLETADO("Pago de cita completado",
            "El cliente completó el pago y la cita está confirmada.", "Ver cita"),
    CITA_CANCELADA_POR_CLIENTE("El cliente canceló la cita",
            "El horario se liberó y ya puede volver a ofrecerse.", "Ver cita"),
    CITA_CANCELADA_POR_PROFESIONAL("Tu cita fue cancelada",
            "El profesional canceló la cita. Puedes solicitar otro horario disponible.", "Ver mi cita"),
    CITA_REPROGRAMACION_SOLICITADA("Solicitud de reprogramación",
            "El cliente propuso un nuevo horario. Revisa la solicitud en tu agenda.", "Ver solicitud"),
    CITA_REPROGRAMACION_PROPUESTA("Nuevo horario propuesto",
            "El profesional propuso otro horario. Revísalo y responde antes de que venza.", "Ver mi cita"),
    CITA_REPROGRAMACION_ACEPTADA("Reprogramación aceptada",
            "El cliente aceptó el horario propuesto. La cita quedó confirmada.", "Ver cita"),
    CITA_REPROGRAMACION_RECHAZADA("Reprogramación rechazada",
            "El cliente rechazó el horario propuesto y la cita fue liberada.", "Ver cita"),
    CITA_COMPLETADA("Cita completada",
            "La cita se marcó como completada. Gracias por participar.", "Ver mi cita");

    private final String title;
    private final String message;
    private final String actionLabel;
}

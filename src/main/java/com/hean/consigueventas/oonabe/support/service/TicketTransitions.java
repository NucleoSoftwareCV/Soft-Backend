package com.hean.consigueventas.oonabe.support.service;

import com.hean.consigueventas.oonabe.support.enums.TicketStatus;
import java.util.*;

public final class TicketTransitions {
    private TicketTransitions() {}

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED =
            Map.of(
                    TicketStatus.PENDIENTE, Set.of(TicketStatus.EN_REVISION),
                    TicketStatus.ASIGNADO, Set.of(TicketStatus.EN_REVISION, TicketStatus.ESPERANDO_USUARIO),
                    TicketStatus.EN_REVISION,
                    Set.of(TicketStatus.ESPERANDO_USUARIO, TicketStatus.RESUELTO),
                    TicketStatus.ESPERANDO_USUARIO, Set.of(TicketStatus.EN_REVISION),
                    TicketStatus.RESUELTO, Set.of(TicketStatus.CERRADO, TicketStatus.REABIERTO),
                    TicketStatus.CERRADO, Set.of(TicketStatus.REABIERTO),
                    TicketStatus.REABIERTO,
                    Set.of(
                            TicketStatus.EN_REVISION, TicketStatus.ESPERANDO_USUARIO));

    public static boolean allows(TicketStatus from, TicketStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
}

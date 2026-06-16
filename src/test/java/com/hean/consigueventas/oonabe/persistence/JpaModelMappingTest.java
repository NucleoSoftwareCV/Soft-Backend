package com.hean.consigueventas.oonabe.persistence;

import com.hean.consigueventas.oonabe.admin.entity.AuditLog;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.content.entity.HomeSection;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.interaction.entity.Favorite;
import com.hean.consigueventas.oonabe.payment.entity.Payment;
import com.hean.consigueventas.oonabe.profile.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.service.entity.OneToOneService;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JpaModelMappingTest {

    @Test
    void coreDomainEntitiesUseExpectedPostgreSqlTableNames() {
        assertEntityTable(CustomerProfile.class, "perfil_clientes");
        assertEntityTable(Event.class, "eventos");
        assertEntityTable(OneToOneService.class, "servicios_1a1");
        assertEntityTable(EventBooking.class, "reservas_evento");
        assertEntityTable(Payment.class, "pagos");
        assertEntityTable(HomeSection.class, "secciones_inicio");
        assertEntityTable(Favorite.class, "favoritos");
        assertEntityTable(AuditLog.class, "auditoria");
    }

    private static void assertEntityTable(Class<?> entityType, String tableName) {
        assertThat(entityType.getAnnotation(Entity.class)).isNotNull();
        assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(tableName);
    }
}

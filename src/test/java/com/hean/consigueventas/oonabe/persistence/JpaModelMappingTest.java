package com.hean.consigueventas.oonabe.persistence;

import com.hean.consigueventas.oonabe.admin.entity.AuditLog;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.catalog.entity.City;
import com.hean.consigueventas.oonabe.content.entity.HomeSection;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.interaction.entity.Favorite;
import com.hean.consigueventas.oonabe.payment.entity.Payment;
import com.hean.consigueventas.oonabe.profile.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.service.entity.OneToOneService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JpaModelMappingTest {

    @Test
    void coreDomainEntitiesUseExpectedPostgreSqlTableNames() {
        assertEntityTable(City.class, "cities");
        assertEntityTable(CustomerProfile.class, "customer_profiles");
        assertEntityTable(Event.class, "events");
        assertEntityTable(OneToOneService.class, "one_to_one_services");
        assertEntityTable(EventBooking.class, "event_bookings");
        assertEntityTable(Payment.class, "payments");
        assertEntityTable(HomeSection.class, "home_sections");
        assertEntityTable(Favorite.class, "favorites");
        assertEntityTable(AuditLog.class, "audit_logs");
    }

    @Test
    void cityActiveFieldUsesEnglishColumnName() throws NoSuchFieldException {
        Column activeColumn = City.class.getDeclaredField("isActive").getAnnotation(Column.class);

        assertThat(activeColumn).isNotNull();
        assertThat(activeColumn.name()).isEqualTo("active");
        assertThat(activeColumn.nullable()).isFalse();
    }

    private static void assertEntityTable(Class<?> entityType, String tableName) {
        assertThat(entityType.getAnnotation(Entity.class)).isNotNull();
        assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(tableName);
    }
}

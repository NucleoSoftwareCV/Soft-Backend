package com.hean.consigueventas.oonabe.persistence;

import com.hean.consigueventas.oonabe.audit.entity.AuditLog;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.content.entity.HomeSection;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.interaction.entity.Favorite;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import com.hean.consigueventas.oonabe.masterdata.entity.Technique;
import com.hean.consigueventas.oonabe.masterdata.entity.WorkTopic;
import com.hean.consigueventas.oonabe.payment.entity.Payment;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalTechnique;
import com.hean.consigueventas.oonabe.profileProfesional.entity.ProfessionalWorkTopic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.springframework.data.jpa.repository.EntityGraph;
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
        assertEntityTable(ProfessionalFollow.class, "professional_follows");
        assertEntityTable(AuditLog.class, "audit_logs");
    }

    @Test
    void cityActiveFieldUsesEnglishColumnName() throws NoSuchFieldException {
        Column activeColumn = City.class.getDeclaredField("isActive").getAnnotation(Column.class);

        assertThat(activeColumn).isNotNull();
        assertThat(activeColumn.name()).isEqualTo("active");
        assertThat(activeColumn.nullable()).isFalse();
    }

    @Test
    void professionalTaxonomyEntitiesUseEnglishTableNames() {
        assertEntityTable(Technique.class, "techniques");
        assertEntityTable(WorkTopic.class, "work_topics");
        assertEntityTable(ProfessionalTechnique.class, "professional_techniques");
        assertEntityTable(ProfessionalWorkTopic.class, "professional_work_topics");
    }

    @Test
    void mutablePublicationsUseOptimisticLocking() throws NoSuchFieldException {
        assertThat(Event.class.getDeclaredField("version").getAnnotation(Version.class)).isNotNull();
        assertThat(EventOccurrence.class.getDeclaredField("version").getAnnotation(Version.class)).isNotNull();
        assertThat(OneToOneService.class.getDeclaredField("version").getAnnotation(Version.class)).isNotNull();
    }

    @Test
    void eventDetailGraphDoesNotJoinElementCollections() throws NoSuchMethodException {
        EntityGraph graph = EventRepository.class
                .getMethod("findDetailById", Long.class)
                .getAnnotation(EntityGraph.class);

        assertThat(graph).isNotNull();
        assertThat(graph.attributePaths())
                .doesNotContain("includes", "highlights", "whatToBring")
                .contains("category", "specialist", "occurrences");
    }

    @Test
    void professionalFollowPreventsDuplicateRelationships() {
        Table table = ProfessionalFollow.class.getAnnotation(Table.class);

        assertThat(table.uniqueConstraints())
                .extracting(UniqueConstraint::name)
                .contains("uk_professional_follow_user_specialist");
        assertThat(table.uniqueConstraints()[0].columnNames())
                .containsExactly("user_id", "specialist_profile_id");
    }

    private static void assertEntityTable(Class<?> entityType, String tableName) {
        assertThat(entityType.getAnnotation(Entity.class)).isNotNull();
        assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(tableName);
    }
}

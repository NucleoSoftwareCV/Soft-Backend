package com.hean.consigueventas.oonabe.booking;

import com.hean.consigueventas.oonabe.appointment.*;
import static com.hean.consigueventas.oonabe.appointment.AppointmentDtos.*;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @Import(AppointmentIntegrationTest.FixedTime.class)
class AppointmentConcurrencyTest {
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        String postgres=System.getenv("OONA_APPOINTMENT_TEST_URL");
        if(postgres!=null && postgres.startsWith("jdbc:postgresql://127.0.0.1:55492/")) {
            registry.add("spring.datasource.url",()->postgres);
            registry.add("spring.datasource.username",()->"oona_appointment_check");
            registry.add("spring.datasource.password",()->"");
            registry.add("spring.datasource.driver-class-name",()->"org.postgresql.Driver");
            registry.add("spring.jpa.database-platform",()->"org.hibernate.dialect.PostgreSQLDialect");
        } else registry.add("spring.datasource.url",()->"jdbc:h2:mem:appointment_concurrency;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=15000");
        registry.add("spring.jpa.hibernate.ddl-auto",()->"create-drop");
    }
    @Autowired AppointmentService appointments;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactions;
    Long client1,client2,professional1,service1,service2;
    final LocalDate day=LocalDate.of(2026,11,9);
    final Instant start=Instant.parse("2026-11-09T14:00:00Z");
    @BeforeEach void setup() {
        new TransactionTemplate(transactions).executeWithoutResult(tx->{
            var first=BookingFixtures.create(em,"a"+UUID.randomUUID().toString().substring(0,8));
            var second=BookingFixtures.create(em,"b"+UUID.randomUUID().toString().substring(0,8));
            client1=first.client().getId(); client2=second.client().getId(); professional1=first.professional().getId();
            service1=createService(first); service2=createService(second);
        });
    }
    Long createService(BookingFixtures.Fixture fixture) {
        var service=new OneToOneService(); service.setSpecialist(fixture.occurrence().getEvent().getSpecialist());
        service.setSlug("concurrency-"+UUID.randomUUID()); service.setTitle("Consulta de prueba"); service.setDescription("Verificación concurrente");
        service.setDurationMinutes(60); service.setModality(SessionModality.ONLINE); service.setPrice(BigDecimal.TEN); service.setStatus(PublicationStatus.PUBLICADO);
        em.persist(service); em.flush();
        appointments.addRule(fixture.professional().getId(),new RuleInput(1,LocalTime.of(9,0),LocalTime.of(12,0),day,null,null));
        return service.getId();
    }
    boolean request(Long user,Long service) {
        try { appointments.request(user,new Request(service,SessionModality.ONLINE,null,start,"Verificación de concurrencia")); return true; }
        catch(BookingConflictException expected) { return false; }
    }
    void concurrently(Supplier<Boolean> first,Supplier<Boolean> second) throws Exception {
        var executor=Executors.newFixedThreadPool(2); var ready=new CountDownLatch(2); var go=new CountDownLatch(1);
        try {
            var tasks=List.of(first,second).stream().map(task->executor.submit(()->{ ready.countDown(); if(!go.await(10,TimeUnit.SECONDS)) throw new IllegalStateException("No se iniciaron las dos solicitudes"); return task.get(); })).toList();
            assertThat(ready.await(10,TimeUnit.SECONDS)).isTrue(); go.countDown();
            var outcomes=new ArrayList<Boolean>(); for(var task:tasks) outcomes.add(task.get(20,TimeUnit.SECONDS));
            assertThat(outcomes).containsExactlyInAnyOrder(true,false);
        } finally { executor.shutdownNow(); }
    }
    @Test void twoClientsCannotAcquireTheSameProfessionalSlot() throws Exception {
        concurrently(()->request(client1,service1),()->request(client2,service1));
        assertThat(appointments.agenda(professional1,day)).hasSize(1);
    }
    @Test void oneClientCannotAcquireTwoOverlappingAppointmentsWithDifferentProfessionals() throws Exception {
        concurrently(()->request(client1,service1),()->request(client1,service2));
    }
}

package com.hean.consigueventas.oonabe.campaign.config;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import com.hean.consigueventas.oonabe.campaign.entity.CampaignFaq;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaign;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEvent;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEventRepository;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignRepository;
import com.hean.consigueventas.oonabe.common.config.TimeConfig;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.event.specification.EventSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Datos de ejemplo del sorteo «sorteo-bienestar» para entornos locales.
 * Corre cuando la aplicación ya está lista, después de los seeders base (que crean los eventos),
 * con una ventana de participación abierta relativa a la fecha actual. Es idempotente por slug.
 * No corre en el perfil de test para no alterar los datos de las pruebas.
 */
@Component
@Profile({"default", "dev", "local", "postgres"})
@Slf4j
public class CampaignDataSeeder {

    static final String SLUG = "sorteo-bienestar";
    private static final int SAMPLE_EVENTS = 8;

    private final PromoCampaignRepository campaigns;
    private final PromoCampaignEventRepository campaignEvents;
    private final EventRepository events;
    private final Clock clock;

    public CampaignDataSeeder(
            PromoCampaignRepository campaigns,
            PromoCampaignEventRepository campaignEvents,
            EventRepository events,
            Clock clock) {
        this.campaigns = campaigns;
        this.campaignEvents = campaignEvents;
        this.events = events;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (campaigns.existsBySlug(SLUG)) {
            return;
        }
        LocalDate today = LocalDate.now(clock.withZone(TimeConfig.BUSINESS_ZONE));
        ZonedDateTime startsAt = today.minusDays(1).atTime(LocalTime.of(14, 0)).atZone(TimeConfig.BUSINESS_ZONE);
        ZonedDateTime endsAt = today.plusDays(14).atTime(LocalTime.of(23, 59)).atZone(TimeConfig.BUSINESS_ZONE);
        ZonedDateTime drawAt = endsAt.toLocalDate().plusDays(5).atTime(LocalTime.NOON).atZone(TimeConfig.BUSINESS_ZONE);

        PromoCampaign campaign = new PromoCampaign();
        campaign.setSlug(SLUG);
        campaign.setTitle("Sorteamos un año de bienestar gratis");
        campaign.setSubtitle("Sorteo · 600 € en crédito Oona");
        campaign.setDescription("Reserva una experiencia elegible en Oona durante el periodo de participación "
                + "y entra en el sorteo de 600 € para disfrutar de bienestar durante 12 meses.");
        campaign.setBadgeLabel("Participa en el sorteo");
        campaign.setPrizeAmount(new BigDecimal("600.00"));
        campaign.setPrizeCurrency("EUR");
        campaign.setPrizeDescription("Crédito Oona para disfrutar de experiencias de bienestar durante 12 meses "
                + "desde su activación, en una o varias reservas elegibles.");
        campaign.setParticipationStartsAt(startsAt.toInstant());
        campaign.setParticipationEndsAt(endsAt.toInstant());
        campaign.setDrawAt(drawAt.toInstant());
        campaign.setPrizeActivationDeadline(drawAt.plusDays(30).toInstant());
        campaign.setParticipationConditions("Participa cada reserva confirmada de una experiencia con el distintivo "
                + "«Participa en el sorteo» hecha dentro del periodo de participación. Cada plaza reservada cuenta "
                + "como una participación. Si la reserva se cancela, sus participaciones se anulan.");
        campaign.setLegalTerms("Promoción organizada por Oona. Participación gratuita para personas mayores de edad "
                + "con cuenta en Oona. El sorteo se realiza entre todas las participaciones válidas al cierre.");
        campaign.setStatus(CampaignStatus.PUBLICADA);
        campaign.getFaqs().addAll(List.of(
                new CampaignFaq("¿Quién puede participar?",
                        "Cualquier persona mayor de edad con cuenta en Oona que reserve una experiencia participante."),
                new CampaignFaq("¿Qué reservas participan?",
                        "Las reservas confirmadas de experiencias con el distintivo, hechas dentro del periodo de participación."),
                new CampaignFaq("¿Puedo participar varias veces?",
                        "Sí. Cada plaza reservada es una participación: cuantas más reservas, más oportunidades."),
                new CampaignFaq("¿Tengo que hacer algo más?",
                        "No. La participación se registra automáticamente cuando tu reserva queda confirmada."),
                new CampaignFaq("¿Y si cancelo mi reserva?",
                        "Las participaciones de una reserva cancelada dejan de ser válidas."),
                new CampaignFaq("¿Cómo se elige a la persona ganadora?",
                        "Por sorteo aleatorio entre todas las participaciones válidas en la fecha indicada.")));
        PromoCampaign saved = campaigns.save(campaign);

        List<Event> sample = events.findAll(
                EventSpecification.isPublished(),
                PageRequest.of(0, SAMPLE_EVENTS, Sort.by("id"))).getContent();
        campaignEvents.saveAll(sample.stream().map(event -> new PromoCampaignEvent(saved, event)).toList());
        log.info("Campaña de ejemplo '{}' creada con {} experiencias", SLUG, sample.size());
    }
}

package com.hean.consigueventas.oonabe.campaign.service;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignEntryStatus;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.AdminEntry;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.AdminResponse;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.Detail;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.Faq;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.Summary;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.UpsertRequest;
import com.hean.consigueventas.oonabe.campaign.entity.CampaignFaq;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaign;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEvent;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEntryRepository;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEventRepository;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignRepository;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PromoCampaignService {

    static final String DEFAULT_BADGE_LABEL = "Participa en el sorteo";

    private final PromoCampaignRepository campaigns;
    private final PromoCampaignEventRepository campaignEvents;
    private final PromoCampaignEntryRepository entries;
    private final EventRepository events;
    private final Clock clock;

    public PromoCampaignService(
            PromoCampaignRepository campaigns,
            PromoCampaignEventRepository campaignEvents,
            PromoCampaignEntryRepository entries,
            EventRepository events,
            Clock clock) {
        this.campaigns = campaigns;
        this.campaignEvents = campaignEvents;
        this.entries = entries;
        this.events = events;
        this.clock = clock;
    }

    public List<Summary> visibleCampaigns() {
        return campaigns.findVisibleNotClosed(CampaignStatus.PUBLICADA, clock.instant()).stream()
                .map(this::summary)
                .toList();
    }

    /** Las campañas en borrador no son públicas; las finalizadas siguen consultables. */
    public Detail publicDetail(String slug) {
        PromoCampaign campaign = campaigns.findBySlug(slug)
                .filter(c -> c.getStatus() != CampaignStatus.BORRADOR)
                .orElseThrow(() -> notFound(slug));
        return detail(campaign);
    }

    public List<AdminResponse> adminList() {
        return campaigns.findAllByOrderByParticipationStartsAtDesc().stream().map(this::admin).toList();
    }

    public AdminResponse adminGet(Long id) {
        return admin(get(id));
    }

    @Transactional
    public AdminResponse create(UpsertRequest request) {
        if (campaigns.existsBySlug(request.slug())) {
            throw new BusinessLogicException("Ya existe una campaña con el slug: " + request.slug());
        }
        PromoCampaign campaign = new PromoCampaign();
        apply(campaign, request);
        return admin(campaigns.save(campaign));
    }

    @Transactional
    public AdminResponse update(Long id, UpsertRequest request) {
        PromoCampaign campaign = get(id);
        if (campaigns.existsBySlugAndIdNot(request.slug(), id)) {
            throw new BusinessLogicException("Ya existe una campaña con el slug: " + request.slug());
        }
        apply(campaign, request);
        return admin(campaigns.saveAndFlush(campaign));
    }

    @Transactional
    public AdminResponse updateStatus(Long id, CampaignStatus status) {
        PromoCampaign campaign = get(id);
        campaign.setStatus(status);
        return admin(campaigns.saveAndFlush(campaign));
    }

    /** Reemplaza el conjunto de experiencias incluidas. */
    @Transactional
    public AdminResponse replaceEvents(Long id, List<Long> eventIds) {
        PromoCampaign campaign = get(id);
        Set<Long> requested = new LinkedHashSet<>(eventIds);
        Map<Long, Event> found = events.findAllById(requested).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        List<Long> missing = requested.stream().filter(eventId -> !found.containsKey(eventId)).toList();
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("No existen los eventos: " + missing);
        }
        campaignEvents.deleteByCampaignId(campaign.getId());
        PromoCampaign managed = get(id);
        campaignEvents.saveAll(requested.stream().map(eventId -> new PromoCampaignEvent(managed, found.get(eventId))).toList());
        campaignEvents.flush();
        return admin(managed);
    }

    public List<AdminEntry> adminEntries(Long id, CampaignEntryStatus status) {
        PromoCampaign campaign = get(id);
        return entries.findForAdmin(campaign.getId(), status).stream()
                .map(e -> new AdminEntry(
                        e.getCode(),
                        e.getBooking().getCode(),
                        (e.getCustomer().getFirstNames() + " " + e.getCustomer().getLastNames()).trim(),
                        e.getCustomer().getUser().getEmail(),
                        e.getBooking().getOccurrence().getEvent().getId(),
                        e.getBooking().getOccurrence().getEvent().getTitle(),
                        e.getTicketCount(),
                        e.getStatus(),
                        e.getCreatedAt(),
                        e.getVoidedAt(),
                        e.getVoidReason()))
                .toList();
    }

    private void apply(PromoCampaign campaign, UpsertRequest request) {
        campaign.setSlug(request.slug());
        campaign.setTitle(request.title().strip());
        campaign.setSubtitle(blankToNull(request.subtitle()));
        campaign.setDescription(request.description().strip());
        campaign.setBadgeLabel(request.badgeLabel() == null || request.badgeLabel().isBlank()
                ? DEFAULT_BADGE_LABEL : request.badgeLabel().strip());
        campaign.setHeroImageUrl(blankToNull(request.heroImageUrl()));
        campaign.setPrizeAmount(request.prizeAmount());
        campaign.setPrizeCurrency(request.prizeCurrency() == null ? "EUR" : request.prizeCurrency());
        campaign.setPrizeDescription(blankToNull(request.prizeDescription()));
        campaign.setParticipationStartsAt(request.participationStartsAt());
        campaign.setParticipationEndsAt(request.participationEndsAt());
        campaign.setDrawAt(request.drawAt());
        campaign.setPrizeActivationDeadline(request.prizeActivationDeadline());
        campaign.setParticipationConditions(blankToNull(request.participationConditions()));
        campaign.setLegalTerms(blankToNull(request.legalTerms()));
        campaign.setMaxEntriesPerCustomer(request.maxEntriesPerCustomer());
        List<CampaignFaq> faqs = new ArrayList<>();
        if (request.faqs() != null) {
            request.faqs().forEach(faq -> faqs.add(new CampaignFaq(faq.question().strip(), faq.answer().strip())));
        }
        campaign.getFaqs().clear();
        campaign.getFaqs().addAll(faqs);
    }

    private PromoCampaign get(Long id) {
        return campaigns.findById(id).orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada: " + id));
    }

    private Summary summary(PromoCampaign c) {
        return new Summary(
                c.getSlug(),
                c.getTitle(),
                c.getSubtitle(),
                c.getBadgeLabel(),
                c.getHeroImageUrl(),
                c.getPrizeAmount(),
                c.getPrizeCurrency(),
                c.getParticipationStartsAt(),
                c.getParticipationEndsAt(),
                c.getDrawAt(),
                c.phaseAt(clock.instant()));
    }

    private Detail detail(PromoCampaign c) {
        return new Detail(
                summary(c),
                c.getDescription(),
                c.getPrizeDescription(),
                c.getPrizeActivationDeadline(),
                c.getParticipationConditions(),
                c.getLegalTerms(),
                c.getMaxEntriesPerCustomer(),
                c.getFaqs().stream().map(f -> new Faq(f.getQuestion(), f.getAnswer())).toList(),
                campaignEvents.countByCampaignId(c.getId()));
    }

    private AdminResponse admin(PromoCampaign c) {
        return new AdminResponse(
                c.getId(),
                c.getStatus(),
                detail(c),
                campaignEvents.findEventIdsByCampaignId(c.getId()),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static ResourceNotFoundException notFound(String slug) {
        return new ResourceNotFoundException("Campaña no encontrada: " + slug);
    }
}

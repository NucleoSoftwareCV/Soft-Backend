package com.hean.consigueventas.oonabe.campaign.service;

import com.hean.consigueventas.oonabe.campaign.domain.CampaignEntryStatus;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignPhase;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.AdminEntry;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.AdminEvent;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.AdminResponse;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.AdminWinner;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.Detail;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.Faq;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.Summary;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.UpsertRequest;
import com.hean.consigueventas.oonabe.campaign.entity.CampaignFaq;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaign;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEntry;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEvent;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEntryRepository;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEventRepository;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignRepository;
import com.hean.consigueventas.oonabe.common.enums.EventStatus;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.profileProfesional.service.LocalImageStorageService;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PromoCampaignService {

    static final String DEFAULT_BADGE_LABEL = "Participa en el sorteo";

    private final PromoCampaignRepository campaigns;
    private final PromoCampaignEventRepository campaignEvents;
    private final PromoCampaignEntryRepository entries;
    private final EventRepository events;
    private final UserRepository users;
    private final LocalImageStorageService images;
    private final RandomGenerator drawRandom;
    private final Clock clock;

    public PromoCampaignService(
            PromoCampaignRepository campaigns,
            PromoCampaignEventRepository campaignEvents,
            PromoCampaignEntryRepository entries,
            EventRepository events,
            UserRepository users,
            LocalImageStorageService images,
            RandomGenerator campaignDrawRandom,
            Clock clock) {
        this.campaigns = campaigns;
        this.campaignEvents = campaignEvents;
        this.entries = entries;
        this.events = events;
        this.users = users;
        this.images = images;
        this.drawRandom = campaignDrawRandom;
        this.clock = clock;
    }

    /** Campañas publicadas sin cerrar: primero las abiertas que cierran antes, luego las próximas. */
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
        List<PromoCampaign> all = campaigns.findAllByOrderByParticipationStartsAtDesc();
        Map<Long, EntryStats> stats = stats(all.stream().map(PromoCampaign::getId).toList());
        return all.stream().map(c -> admin(c, stats.getOrDefault(c.getId(), EntryStats.EMPTY))).toList();
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

    /**
     * Una campaña finalizada no se edita. Con participaciones registradas se protege lo que las define:
     * slug, inicio de la participación y premio; el cierre solo puede ampliarse.
     */
    @Transactional
    public AdminResponse update(Long id, UpsertRequest request) {
        PromoCampaign campaign = get(id);
        requireNotFinished(campaign);
        if (campaigns.existsBySlugAndIdNot(request.slug(), id)) {
            throw new BusinessLogicException("Ya existe una campaña con el slug: " + request.slug());
        }
        if (entries.existsByCampaignId(id)) {
            if (!campaign.getSlug().equals(request.slug())) {
                throw locked("el slug");
            }
            if (!campaign.getParticipationStartsAt().equals(request.participationStartsAt())) {
                throw locked("la fecha de inicio de la participación");
            }
            String currency = request.prizeCurrency() == null ? "EUR" : request.prizeCurrency();
            if (campaign.getPrizeAmount().compareTo(request.prizeAmount()) != 0
                    || !campaign.getPrizeCurrency().equals(currency)) {
                throw locked("el premio");
            }
            if (request.participationEndsAt().isBefore(campaign.getParticipationEndsAt())) {
                throw new BusinessLogicException(
                        "La campaña ya tiene participaciones: la fecha de cierre solo puede ampliarse");
            }
        }
        String previousImage = campaign.getHeroImageUrl();
        apply(campaign, request);
        AdminResponse saved = admin(campaigns.saveAndFlush(campaign));
        if (previousImage != null && !previousImage.equals(campaign.getHeroImageUrl())) {
            images.deleteImage(previousImage);
        }
        return saved;
    }

    /** Guarda la imagen principal en el almacenamiento del backend y borra la anterior. */
    @Transactional
    public AdminResponse uploadImage(Long id, MultipartFile file) {
        PromoCampaign campaign = get(id);
        requireNotFinished(campaign);
        String previousImage = campaign.getHeroImageUrl();
        campaign.setHeroImageUrl(images.saveCampaignImage(file, id));
        AdminResponse saved = admin(campaigns.saveAndFlush(campaign));
        images.deleteImage(previousImage);
        return saved;
    }

    /** BORRADOR → PUBLICADA → FINALIZADA; volver a borrador solo sin participaciones. FINALIZADA es definitiva. */
    @Transactional
    public AdminResponse updateStatus(Long id, CampaignStatus status) {
        PromoCampaign campaign = get(id);
        CampaignStatus current = campaign.getStatus();
        if (current == status) {
            return admin(campaign);
        }
        requireNotFinished(campaign);
        switch (status) {
            case PUBLICADA -> {
                if (campaignEvents.countByCampaignId(id) == 0) {
                    throw new BusinessLogicException("Añade al menos una experiencia antes de publicar la campaña");
                }
                if (campaign.phaseAt(clock.instant()) == CampaignPhase.CERRADA) {
                    throw new BusinessLogicException("No se puede publicar una campaña cuyo periodo de participación ya cerró");
                }
            }
            case BORRADOR -> {
                if (entries.existsByCampaignId(id)) {
                    throw new BusinessLogicException(
                            "La campaña ya tiene participaciones: no puede volver a borrador, solo finalizarse");
                }
            }
            case FINALIZADA -> {
                // Cierre manual sin sorteo; las participaciones quedan como registro.
            }
        }
        campaign.setStatus(status);
        return admin(campaigns.saveAndFlush(campaign));
    }

    /**
     * Reemplaza el conjunto de experiencias incluidas. Las nuevas deben estar publicadas y, si ya hay
     * participaciones, no se puede quitar ninguna de las que ya estaban.
     */
    @Transactional
    public AdminResponse replaceEvents(Long id, List<Long> eventIds) {
        PromoCampaign campaign = get(id);
        requireNotFinished(campaign);
        Set<Long> requested = new LinkedHashSet<>(eventIds);
        Map<Long, Event> found = events.findAllById(requested).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));
        List<Long> missing = requested.stream().filter(eventId -> !found.containsKey(eventId)).toList();
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("No existen los eventos: " + missing);
        }
        Set<Long> current = new HashSet<>(campaignEvents.findEventIdsByCampaignId(id));
        List<Long> notPublished = requested.stream()
                .filter(eventId -> !current.contains(eventId))
                .filter(eventId -> found.get(eventId).getStatus() != EventStatus.PUBLICADO)
                .toList();
        if (!notPublished.isEmpty()) {
            throw new BusinessLogicException("Solo se pueden incluir experiencias publicadas: " + notPublished);
        }
        if (entries.existsByCampaignId(id) && !requested.containsAll(current)) {
            throw new BusinessLogicException(
                    "La campaña ya tiene participaciones: solo se pueden añadir experiencias, no quitarlas");
        }
        campaignEvents.deleteByCampaignId(campaign.getId());
        PromoCampaign managed = get(id);
        campaignEvents.saveAll(requested.stream().map(eventId -> new PromoCampaignEvent(managed, found.get(eventId))).toList());
        campaignEvents.flush();
        return admin(managed);
    }

    /** Solo se borran borradores que nunca recibieron participaciones. */
    @Transactional
    public void delete(Long id) {
        PromoCampaign campaign = get(id);
        if (campaign.getStatus() != CampaignStatus.BORRADOR || entries.existsByCampaignId(id)) {
            throw new BusinessLogicException(
                    "Solo se pueden eliminar campañas en borrador sin participaciones; usa «Finalizar» en su lugar");
        }
        campaignEvents.deleteByCampaignId(id);
        campaigns.delete(get(id));
        images.deleteImage(campaign.getHeroImageUrl());
    }

    /**
     * Elige al azar una participación válida, con probabilidad proporcional a sus papeletas, y finaliza la campaña.
     * El bloqueo de la fila evita que dos administradores sorteen a la vez.
     */
    @Transactional
    public AdminResponse draw(Long id, Long adminUserId) {
        PromoCampaign campaign = campaigns.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada: " + id));
        if (campaign.getWinnerEntry() != null) {
            throw new BusinessLogicException("El sorteo de esta campaña ya se realizó");
        }
        if (campaign.getStatus() != CampaignStatus.PUBLICADA) {
            throw new BusinessLogicException("Solo se puede sortear una campaña publicada");
        }
        Instant now = clock.instant();
        if (campaign.phaseAt(now) != CampaignPhase.CERRADA) {
            throw new BusinessLogicException("El sorteo solo puede realizarse cuando cierre el periodo de participación");
        }
        List<PromoCampaignEntry> valid = entries.findForDraw(id, CampaignEntryStatus.VALIDA);
        long totalTickets = valid.stream().mapToLong(PromoCampaignEntry::getTicketCount).sum();
        if (totalTickets <= 0) {
            throw new BusinessLogicException("La campaña no tiene participaciones válidas para sortear");
        }
        campaign.setWinnerEntry(pickWeighted(valid, drawRandom.nextLong(totalTickets)));
        campaign.setDrawnAt(now);
        campaign.setDrawnBy(users.getReferenceById(adminUserId));
        campaign.setStatus(CampaignStatus.FINALIZADA);
        return admin(campaigns.saveAndFlush(campaign));
    }

    public List<AdminEntry> adminEntries(Long id, CampaignEntryStatus status) {
        PromoCampaign campaign = get(id);
        return entries.findForAdmin(campaign.getId(), status).stream()
                .map(e -> new AdminEntry(
                        e.getCode(),
                        e.getBooking().getCode(),
                        fullName(e.getCustomer()),
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

    /** {@code ticket} está en [0, suma de papeletas): se recorre acumulando hasta encontrar su participación. */
    static PromoCampaignEntry pickWeighted(List<PromoCampaignEntry> valid, long ticket) {
        long cumulative = 0;
        for (PromoCampaignEntry entry : valid) {
            cumulative += entry.getTicketCount();
            if (ticket < cumulative) {
                return entry;
            }
        }
        throw new IllegalStateException("Papeleta fuera de rango: " + ticket);
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

    private static void requireNotFinished(PromoCampaign campaign) {
        if (campaign.getStatus() == CampaignStatus.FINALIZADA) {
            throw new BusinessLogicException("La campaña está finalizada y ya no se puede modificar");
        }
    }

    private static BusinessLogicException locked(String field) {
        return new BusinessLogicException("La campaña ya tiene participaciones: no se puede cambiar " + field);
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
        PromoCampaignEntry winner = c.getWinnerEntry();
        return new Detail(
                summary(c),
                c.getDescription(),
                c.getPrizeDescription(),
                c.getPrizeActivationDeadline(),
                c.getParticipationConditions(),
                c.getLegalTerms(),
                c.getMaxEntriesPerCustomer(),
                c.getFaqs().stream().map(f -> new Faq(f.getQuestion(), f.getAnswer())).toList(),
                campaignEvents.countByCampaignId(c.getId()),
                winner == null ? null : publicName(winner.getCustomer()),
                c.getDrawnAt());
    }

    private AdminResponse admin(PromoCampaign c) {
        return admin(c, stats(List.of(c.getId())).getOrDefault(c.getId(), EntryStats.EMPTY));
    }

    private AdminResponse admin(PromoCampaign c, EntryStats stats) {
        List<AdminEvent> included = campaignEvents.findEventsByCampaignId(c.getId()).stream()
                .map(e -> new AdminEvent(e.getId(), e.getTitle(), e.getStatus()))
                .toList();
        PromoCampaignEntry winner = c.getWinnerEntry();
        return new AdminResponse(
                c.getId(),
                c.getStatus(),
                c.phaseAt(clock.instant()),
                detail(c),
                included.stream().map(AdminEvent::id).toList(),
                included,
                stats.entryCount(),
                stats.validTickets(),
                stats.participantCount(),
                stats.entryCount() > 0,
                winner == null ? null : new AdminWinner(
                        winner.getCode(),
                        winner.getBooking().getCode(),
                        fullName(winner.getCustomer()),
                        winner.getCustomer().getUser().getEmail(),
                        winner.getTicketCount(),
                        c.getDrawnAt()),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }

    private Map<Long, EntryStats> stats(List<Long> campaignIds) {
        if (campaignIds.isEmpty()) {
            return Map.of();
        }
        return entries.statsByCampaign(campaignIds, CampaignEntryStatus.VALIDA).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> new EntryStats(((Number) row[1]).longValue(), ((Number) row[2]).longValue(),
                                ((Number) row[3]).longValue())));
    }

    private static String fullName(CustomerProfile customer) {
        return (Objects.toString(customer.getFirstNames(), "") + " " + Objects.toString(customer.getLastNames(), "")).trim();
    }

    /** Nombre + inicial del apellido, para publicar el resultado sin exponer datos personales. */
    static String publicName(CustomerProfile customer) {
        String first = Objects.toString(customer.getFirstNames(), "").strip();
        String last = Objects.toString(customer.getLastNames(), "").strip();
        String firstName = first.isEmpty() ? "Participante" : first.split("\\s+")[0];
        return last.isEmpty() ? firstName : firstName + " " + last.substring(0, 1).toUpperCase() + ".";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static ResourceNotFoundException notFound(String slug) {
        return new ResourceNotFoundException("Campaña no encontrada: " + slug);
    }

    private record EntryStats(long entryCount, long validTickets, long participantCount) {
        static final EntryStats EMPTY = new EntryStats(0, 0, 0);
    }
}

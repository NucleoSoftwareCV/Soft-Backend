package com.hean.consigueventas.oonabe.campaign.service;

import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignEntryStatus;
import com.hean.consigueventas.oonabe.campaign.domain.CampaignStatus;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.EntrySummary;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.MyEntries;
import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.MyEntry;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaign;
import com.hean.consigueventas.oonabe.campaign.entity.PromoCampaignEntry;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEntryRepository;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignEventRepository;
import com.hean.consigueventas.oonabe.campaign.repository.PromoCampaignRepository;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Registra y anula participaciones a partir de los cambios de estado de las reservas.
 * Se invoca dentro de la transacción del pago o de la decisión del profesional, que ya
 * tienen bloqueadas la orden y sus reservas, por lo que la comprobación de existencia
 * basta para evitar duplicados; la restricción única es la última barrera.
 */
@Service
public class CampaignEntryService {

    private final PromoCampaignRepository campaigns;
    private final PromoCampaignEventRepository campaignEvents;
    private final PromoCampaignEntryRepository entries;

    public CampaignEntryService(
            PromoCampaignRepository campaigns,
            PromoCampaignEventRepository campaignEvents,
            PromoCampaignEntryRepository entries) {
        this.campaigns = campaigns;
        this.campaignEvents = campaignEvents;
        this.entries = entries;
    }

    /**
     * Crea una participación por reserva confirmada y campaña abierta en {@code at}.
     * Cada reserva aporta tantas participaciones como plazas, recortadas al límite por cliente.
     */
    @Transactional
    public List<EntrySummary> registerForConfirmedBookings(Collection<EventBooking> bookings, Instant at) {
        List<EntrySummary> created = new ArrayList<>();
        for (EventBooking booking : bookings) {
            if (booking.getStatus() != BookingStatus.CONFIRMADA) {
                continue;
            }
            Long eventId = booking.getOccurrence().getEvent().getId();
            for (PromoCampaign campaign : campaignEvents.findOpenCampaignsForEvent(eventId, CampaignStatus.PUBLICADA, at)) {
                if (entries.existsByCampaignIdAndBookingId(campaign.getId(), booking.getId())) {
                    continue;
                }
                int tickets = allowedTickets(campaign, booking);
                if (tickets <= 0) {
                    continue;
                }
                PromoCampaignEntry entry = new PromoCampaignEntry();
                entry.setCode("ENT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                entry.setCampaign(campaign);
                entry.setBooking(booking);
                entry.setCustomer(booking.getCustomer());
                entry.setTicketCount(tickets);
                entry.setStatus(CampaignEntryStatus.VALIDA);
                entry.setCreatedAt(at);
                entries.save(entry);
                created.add(new EntrySummary(campaign.getSlug(), campaign.getTitle(), booking.getCode(), tickets));
            }
        }
        return created;
    }

    /** Anula las participaciones válidas de reservas que dejaron de estar confirmadas. */
    @Transactional
    public int voidForBookings(Collection<EventBooking> bookings, String reason, Instant at) {
        if (bookings.isEmpty()) {
            return 0;
        }
        List<Long> bookingIds = bookings.stream().map(EventBooking::getId).toList();
        List<PromoCampaignEntry> valid = entries.findByBookingIdsAndStatus(bookingIds, CampaignEntryStatus.VALIDA);
        for (PromoCampaignEntry entry : valid) {
            entry.setStatus(CampaignEntryStatus.ANULADA);
            entry.setVoidedAt(at);
            entry.setVoidReason(reason);
        }
        return valid.size();
    }

    @Transactional(readOnly = true)
    public MyEntries myEntries(String slug, Long userId) {
        PromoCampaign campaign = campaigns.findBySlug(slug)
                .filter(c -> c.getStatus() != CampaignStatus.BORRADOR)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada: " + slug));
        List<MyEntry> mine = entries.findForCustomerUser(campaign.getId(), userId).stream()
                .map(e -> new MyEntry(
                        e.getCode(),
                        e.getBooking().getCode(),
                        e.getBooking().getOccurrence().getEvent().getId(),
                        e.getBooking().getOccurrence().getEvent().getTitle(),
                        e.getBooking().getOccurrence().getStartsAt(),
                        e.getTicketCount(),
                        e.getStatus(),
                        e.getCreatedAt()))
                .toList();
        long validTickets = mine.stream()
                .filter(e -> e.status() == CampaignEntryStatus.VALIDA)
                .mapToLong(MyEntry::ticketCount)
                .sum();
        return new MyEntries(campaign.getSlug(), validTickets, mine);
    }

    private int allowedTickets(PromoCampaign campaign, EventBooking booking) {
        int requested = booking.getQuantity() == null ? 1 : booking.getQuantity();
        if (campaign.getMaxEntriesPerCustomer() == null) {
            return requested;
        }
        long used = entries.sumTickets(campaign.getId(), booking.getCustomer().getId(), CampaignEntryStatus.VALIDA);
        return (int) Math.max(0, Math.min(requested, campaign.getMaxEntriesPerCustomer() - used));
    }
}

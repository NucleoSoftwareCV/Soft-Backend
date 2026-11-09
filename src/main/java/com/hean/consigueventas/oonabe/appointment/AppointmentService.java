package com.hean.consigueventas.oonabe.appointment;

import static com.hean.consigueventas.oonabe.appointment.AppointmentDtos.*;
import static com.hean.consigueventas.oonabe.common.config.TimeConfig.BUSINESS_ZONE;
import com.hean.consigueventas.oonabe.booking.entity.SessionBooking;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.common.enums.*;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.masterdata.entity.Location;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import java.time.*;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class AppointmentService {
    private final AppointmentRepository data;
    private final Clock clock;
    private final int holdMinutes;
    public AppointmentService(AppointmentRepository data, Clock clock,
                              @Value("${oona.appointments.hold-minutes:1440}") int holdMinutes) {
        if(holdMinutes<5 || holdMinutes>4320) throw new IllegalArgumentException("La retención de citas debe estar entre 5 y 4320 minutos.");
        this.data=data; this.clock=clock; this.holdMinutes=holdMinutes;
    }
    private LocalDate today() { return clock.instant().atZone(BUSINESS_ZONE).toLocalDate(); }
    private OneToOneService service(Long id) {
        var s=data.find(OneToOneService.class,id); var p=s.getSpecialist();
        if(s.getStatus()!=PublicationStatus.PUBLICADO || p.getApprovalStatus()!=ApprovalStatus.APROBADO
            || p.getPublicationStatus()!=PublicationStatus.PUBLICADO || !p.getUser().isActive() || !p.isShowOneToOneSessions())
            throw new ResourceNotFoundException("La sesión no está disponible.");
        if(s.getDurationMinutes()==null || s.getDurationMinutes()<1 || s.getDurationMinutes()>480)
            throw new IllegalArgumentException("El profesional debe configurar una duración válida.");
        return s;
    }
    public Options options(Long serviceId) {
        var s=service(serviceId);
        var venues=new LinkedHashMap<Long,Venue>();
        var openings=new ArrayList<Opening>();
        for(var a:data.rules(s.getSpecialist().getId())) {
            if(a.getValidFrom().isAfter(today().plusDays(90)) || (a.getValidUntil()!=null && a.getValidUntil().isBefore(today()))
                || Duration.between(a.getStartTime(),a.getEndTime()).toMinutes()<s.getDurationMinutes()) continue;
            if(a.getLocation()==null && s.getModality()==SessionModality.PRESENCIAL) continue;
            if(a.getLocation()!=null && (s.getModality()==SessionModality.ONLINE || !Boolean.TRUE.equals(a.getLocation().getIsActive())
                || (s.getLocation()!=null && !s.getLocation().getId().equals(a.getLocation().getId())))) continue;
            openings.add(new Opening(a.getDayOfWeek(),a.getStartTime(),a.getEndTime(),a.getValidFrom(),a.getValidUntil(),a.getLocation()==null?null:a.getLocation().getId()));
            var l=a.getLocation();
            if(l!=null && Boolean.TRUE.equals(l.getIsActive()) && (a.getValidUntil()==null || !a.getValidUntil().isBefore(today()))
                && (s.getLocation()==null || s.getLocation().getId().equals(l.getId()))) venues.put(l.getId(),venue(l));
        }
        var modes=s.getModality()==SessionModality.AMBAS ? List.of(SessionModality.ONLINE,SessionModality.PRESENCIAL) : List.of(s.getModality());
        return new Options(s.getId(),s.getTitle(),s.getSpecialist().getPublicName(),s.getDurationMinutes(),s.getPrice(),s.getCurrency(),modes,
            List.copyOf(venues.values()),BUSINESS_ZONE.getId(),today(),today().plusDays(90),List.copyOf(openings));
    }
    public Availability availability(Long serviceId, LocalDate date, SessionModality modality, Long locationId) {
        return slots(service(serviceId),date,modality,locationId,null,null);
    }
    private Availability slots(OneToOneService s, LocalDate date, SessionModality modality, Long locationId,
                               Long customerId, Long excludedBookingId) {
        if(date==null || date.isBefore(today()) || date.isAfter(today().plusDays(90))) throw new IllegalArgumentException("Elige una fecha entre hoy y los próximos 90 días.");
        if(modality==null || modality==SessionModality.AMBAS || (s.getModality()!=SessionModality.AMBAS && s.getModality()!=modality))
            throw new IllegalArgumentException("Modalidad no disponible para esta sesión.");
        if(modality==SessionModality.ONLINE && locationId!=null) throw new IllegalArgumentException("Una videollamada no requiere sede.");
        if(modality==SessionModality.PRESENCIAL) {
            if(locationId==null) throw new IllegalArgumentException("Selecciona una sede.");
            var location=data.find(Location.class,locationId);
            if(!Boolean.TRUE.equals(location.getIsActive()) || (s.getLocation()!=null && !s.getLocation().getId().equals(locationId)))
                throw new IllegalArgumentException("Sede no disponible para esta sesión.");
        }
        var dayStart=date.atStartOfDay(BUSINESS_ZONE).toInstant();
        var dayEnd=date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        var now=clock.instant(); var professionalId=s.getSpecialist().getId();
        var busy=data.busy(professionalId,customerId,dayStart,dayEnd,now,excludedBookingId);
        var blocks=data.blocks(professionalId,dayStart,dayEnd);
        var result=new TreeMap<Instant,Slot>();
        for(var a:data.rules(professionalId)) {
            if(a.getDayOfWeek()!=date.getDayOfWeek().getValue() || a.getValidFrom().isAfter(date)
                || (a.getValidUntil()!=null && a.getValidUntil().isBefore(date))) continue;
            Long venueId=a.getLocation()==null?null:a.getLocation().getId();
            if(!Objects.equals(venueId,locationId)) continue;
            var cursor=date.atTime(a.getStartTime()).atZone(BUSINESS_ZONE).toInstant();
            var end=date.atTime(a.getEndTime()).atZone(BUSINESS_ZONE).toInstant();
            while(!cursor.plusSeconds(s.getDurationMinutes()*60L).isAfter(end)) {
                var start=cursor; var finish=start.plusSeconds(s.getDurationMinutes()*60L);
                if(start.isAfter(now) && busy.stream().noneMatch(b->overlap(start,finish,b.getStartsAt(),b.getEndsAt()))
                    && blocks.stream().noneMatch(b->overlap(start,finish,b.getStartsAt(),b.getEndsAt()))) result.put(start,new Slot(start,finish));
                cursor=finish;
            }
        }
        return new Availability(date,BUSINESS_ZONE.getId(),List.copyOf(result.values()));
    }
    private static boolean overlap(Instant start,Instant end,Instant otherStart,Instant otherEnd) { return start.isBefore(otherEnd) && end.isAfter(otherStart); }
    @Transactional
    public Receipt request(Long userId, Request input) {
        // Always lock customer, then professional. This serializes competing requests on both calendars.
        var customer=data.customer(userId);
        if(!customer.getUser().isActive()) throw new AccessDeniedException("Cuenta no disponible.");
        var s=service(input.serviceId()); var p=s.getSpecialist(); data.lock(p);
        if(p.getUser().getId().equals(userId)) throw new IllegalArgumentException("No puedes solicitar una cita contigo mismo.");
        var date=input.startsAt().atZone(BUSINESS_ZONE).toLocalDate();
        var slot=slots(s,date,input.modality(),input.locationId(),customer.getId(),null).slots().stream()
            .filter(item->item.startsAt().equals(input.startsAt())).findFirst()
            .orElseThrow(()->new BookingConflictException("Este horario ya no está disponible. Selecciona otro."));
        var b=new SessionBooking(); b.setCode("CITA-"+UUID.randomUUID().toString().replace("-","").substring(0,24));
        b.setCustomer(customer); b.setService(s); b.setSpecialist(p); b.setStartsAt(slot.startsAt()); b.setEndsAt(slot.endsAt());
        b.setModality(input.modality()); b.setLocation(input.locationId()==null?null:data.find(Location.class,input.locationId()));
        b.setPrice(s.getPrice()); b.setCurrency(s.getCurrency()); b.setStatus(BookingStatus.PENDIENTE);
        b.setCustomerNotes(input.reason().trim()); b.setExpiresAt(clock.instant().plusSeconds(holdMinutes*60L));
        data.save(b); return receipt(b);
    }
    public List<Rule> rules(Long userId) {
        return data.rules(data.professional(userId,false).getId()).stream().map(this::rule).toList();
    }
    @Transactional
    public Rule addRule(Long userId, RuleInput input) {
        var p=data.professional(userId,true);
        if(!input.startTime().isBefore(input.endTime()) || input.startTime().getSecond()!=0 || input.endTime().getSecond()!=0
            || input.startTime().getNano()!=0 || input.endTime().getNano()!=0)
            throw new IllegalArgumentException("Indica una hora final posterior al inicio, en minutos completos.");
        if(input.validUntil()!=null && input.validUntil().isBefore(input.validFrom())) throw new IllegalArgumentException("El rango de fechas no es válido.");
        if(data.rules(p.getId()).size()>=100) throw new IllegalArgumentException("Se alcanzó el límite de franjas. Elimina las que no utilices.");
        Location l=input.locationId()==null?null:data.find(Location.class,input.locationId());
        if(l!=null && !Boolean.TRUE.equals(l.getIsActive())) throw new IllegalArgumentException("Sede inactiva.");
        for(var old:data.rules(p.getId())) {
            if(Objects.equals(old.getLocation()==null?null:old.getLocation().getId(),input.locationId()) && old.getDayOfWeek()==input.dayOfWeek() && old.getStartTime().isBefore(input.endTime()) && old.getEndTime().isAfter(input.startTime())
                && (old.getValidUntil()==null || !old.getValidUntil().isBefore(input.validFrom()))
                && (input.validUntil()==null || !input.validUntil().isBefore(old.getValidFrom())))
                throw new BookingConflictException("Ya existe una franja que se cruza con este horario.");
        }
        var a=new ProfessionalAvailability(); a.setSpecialist(p); a.setDayOfWeek((short)input.dayOfWeek());
        a.setStartTime(input.startTime()); a.setEndTime(input.endTime()); a.setValidFrom(input.validFrom());
        a.setValidUntil(input.validUntil()); a.setLocation(l); data.save(a); return rule(a);
    }
    @Transactional
    public void removeRule(Long userId,Long id) {
        var p=data.professional(userId,true); var a=data.find(ProfessionalAvailability.class,id);
        if(!a.getSpecialist().getId().equals(p.getId())) throw new ResourceNotFoundException("Franja no encontrada.");
        a.setActive(false);
    }
    public List<Receipt> agenda(Long userId, LocalDate date) {
        return agendaRange(userId, date, date);
    }

    public List<Receipt> agendaRange(Long userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from) || to.isAfter(from.plusDays(30))) {
            throw new IllegalArgumentException("Selecciona un periodo de hasta 31 dias.");
        }
        var professional = data.professional(userId, false);
        return data.agenda(professional.getId(), from.atStartOfDay(BUSINESS_ZONE).toInstant(),
                to.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant()).stream().map(this::receipt).toList();
    }

    @Transactional(readOnly = true)
    public List<Receipt> myAppointments(Long userId) {
        return data.forCustomer(userId).stream().map(this::receipt).toList();
    }

    public List<Receipt> pending(Long userId) {
        var professional = data.professional(userId, false);
        return data.pending(professional.getId(), clock.instant()).stream().map(this::receipt).toList();
    }

    @Transactional
    public Receipt cancelMyAppointment(Long userId, Long id, String reason) {
        var specialistId = data.specialistForCustomerAppointment(userId, id);
        data.lock(data.find(SpecialistProfile.class, specialistId));
        var booking = data.find(SessionBooking.class, id);
        if (booking.getStatus() == BookingStatus.CANCELADA) return receipt(booking);

        cancel(booking, clock.instant(), reason, "CLIENTE");
        return receipt(booking);
    }

    @Transactional
    public Receipt rescheduleMyAppointment(Long userId, Long id, Reschedule input) {
        var customer = data.customer(userId);
        var specialistId = data.specialistForCustomerAppointment(userId, id);
        data.lock(data.find(SpecialistProfile.class, specialistId));
        var booking = data.find(SessionBooking.class, id);
        var now = clock.instant();
        boolean pending = booking.getStatus() == BookingStatus.PENDIENTE;
        boolean expired = pending && booking.getExpiresAt() != null && !booking.getExpiresAt().isAfter(now);
        if ((!pending && booking.getStatus() != BookingStatus.CONFIRMADA) || expired || !booking.getStartsAt().isAfter(now)) {
            throw new BookingConflictException("Solo puedes reprogramar citas futuras y vigentes.");
        }
        if (booking.getStartsAt().equals(input.startsAt()) && booking.getModality() == input.modality()
                && Objects.equals(booking.getLocation() == null ? null : booking.getLocation().getId(), input.locationId())) {
            throw new IllegalArgumentException("Selecciona un horario o modalidad diferente.");
        }
        var service = service(booking.getService().getId());
        var date = input.startsAt().atZone(BUSINESS_ZONE).toLocalDate();
        var slot = slots(service, date, input.modality(), input.locationId(), customer.getId(), booking.getId())
                .slots().stream().filter(item -> item.startsAt().equals(input.startsAt())).findFirst()
                .orElseThrow(() -> new BookingConflictException("Este horario ya no está disponible. Selecciona otro."));

        booking.setPreviousStartsAt(booking.getStartsAt());
        booking.setPreviousEndsAt(booking.getEndsAt());
        booking.setStartsAt(slot.startsAt());
        booking.setEndsAt(slot.endsAt());
        booking.setModality(input.modality());
        booking.setLocation(input.locationId() == null ? null : data.find(Location.class, input.locationId()));
        booking.setStatus(BookingStatus.PENDIENTE);
        booking.setExpiresAt(now.plusSeconds(holdMinutes * 60L));
        booking.setRescheduledAt(now);
        booking.setRescheduleReason(input.reason().trim());
        booking.setCancelledAt(null);
        booking.setCancellationReason(null);
        booking.setCancelledBy(null);
        return receipt(booking);
    }

    private void cancel(SessionBooking booking, Instant now, String reason, String actor) {
        boolean pending = booking.getStatus() == BookingStatus.PENDIENTE;
        boolean expired = pending && booking.getExpiresAt() != null && !booking.getExpiresAt().isAfter(now);
        if ((!pending && booking.getStatus() != BookingStatus.CONFIRMADA)
                || expired || !booking.getStartsAt().isAfter(now)) {
            throw new BookingConflictException("Solo puedes cancelar citas futuras y vigentes.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Indica el motivo de la cancelación.");
        }
        booking.setStatus(BookingStatus.CANCELADA);
        booking.setCancelledAt(now);
        booking.setCancellationReason(reason.trim());
        booking.setCancelledBy(actor);
    }

    @Transactional
    public Receipt decide(Long userId,Long id,BookingStatus status,String reason) {
        var p=data.professional(userId,true); var b=data.find(SessionBooking.class,id);
        if(!b.getSpecialist().getId().equals(p.getId()) || b.getOrderItem()!=null) throw new ResourceNotFoundException("Solicitud no encontrada.");
        var now=clock.instant();
        if(b.getStatus()==BookingStatus.PENDIENTE && b.getExpiresAt()!=null && !b.getExpiresAt().isAfter(now))
            throw new BookingConflictException("La solicitud ha vencido.");
        if(status==BookingStatus.CONFIRMADA && b.getStatus()==BookingStatus.PENDIENTE && b.getStartsAt().isAfter(now)) { b.setStatus(status); b.setExpiresAt(null); }
        else if(status==BookingStatus.CANCELADA) cancel(b, now, reason, "PROFESIONAL");
        else if(status==BookingStatus.COMPLETADA && b.getStatus()==BookingStatus.CONFIRMADA && !b.getEndsAt().isAfter(now)) b.setStatus(status);
        else throw new BookingConflictException("El cambio de estado no es válido para esta cita.");
        return receipt(b);
    }
    private Rule rule(ProfessionalAvailability a) { return new Rule(a.getId(),a.getDayOfWeek(),a.getStartTime(),a.getEndTime(),a.getValidFrom(),a.getValidUntil(),venue(a.getLocation())); }
    private Venue venue(Location l) { return l==null?null:new Venue(l.getId(),l.getName(),l.getAddress()); }
    private Receipt receipt(SessionBooking b) {
        String status=b.getStatus().name();
        if(b.getStatus()==BookingStatus.PENDIENTE && b.getExpiresAt()!=null && !b.getExpiresAt().isAfter(clock.instant())) status="EXPIRADA";
        return new Receipt(b.getId(),b.getCode(),b.getService().getId(),b.getService().getTitle(),b.getSpecialist().getPublicName(),
            b.getCustomer().getFirstNames()+" "+b.getCustomer().getLastNames(),b.getStartsAt(),b.getEndsAt(),b.getModality(),venue(b.getLocation()),
            b.getPrice(),b.getCurrency(),status,b.getExpiresAt(),b.getCustomerNotes(),b.getCancelledAt(),
            b.getCancellationReason(),b.getCancelledBy(),b.getRescheduledAt(),b.getPreviousStartsAt(),
            b.getPreviousEndsAt(),b.getRescheduleReason());
    }
}

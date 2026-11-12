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
            openings.add(new Opening(a.getDayOfWeek(),a.getStartTime(),a.getEndTime(),a.getValidFrom(),a.getValidUntil(),
                    a.getLocation()==null?null:a.getLocation().getId(),a.getBreakMinutes(),a.getVirtualUrl()));
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
                cursor=finish.plusSeconds(a.getBreakMinutes()*60L);
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
        b.setVirtualUrl(input.modality()==SessionModality.ONLINE ? virtualUrlForSlot(p.getId(), date, input.startsAt()) : null);
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
        String virtualUrl=normalizeVirtualUrl(input.virtualUrl());
        if(l!=null && virtualUrl!=null) throw new IllegalArgumentException("Una franja presencial no necesita enlace de videollamada.");
        for(var old:data.rules(p.getId())) {
            if(Objects.equals(old.getLocation()==null?null:old.getLocation().getId(),input.locationId()) && old.getDayOfWeek()==input.dayOfWeek() && old.getStartTime().isBefore(input.endTime()) && old.getEndTime().isAfter(input.startTime())
                && (old.getValidUntil()==null || !old.getValidUntil().isBefore(input.validFrom()))
                && (input.validUntil()==null || !input.validUntil().isBefore(old.getValidFrom())))
                throw new BookingConflictException("Ya existe una franja que se cruza con este horario.");
        }
        var a=new ProfessionalAvailability(); a.setSpecialist(p); a.setDayOfWeek((short)input.dayOfWeek());
        a.setStartTime(input.startTime()); a.setEndTime(input.endTime()); a.setValidFrom(input.validFrom());
        a.setValidUntil(input.validUntil()); a.setLocation(l); a.setBreakMinutes(input.breakMinutes());
        a.setVirtualUrl(virtualUrl); data.save(a); return rule(a);
    }
    @Transactional
    public void removeRule(Long userId,Long id) {
        var p=data.professional(userId,true); var a=data.find(ProfessionalAvailability.class,id);
        if(!a.getSpecialist().getId().equals(p.getId())) throw new ResourceNotFoundException("Franja no encontrada.");
        a.setActive(false);
    }

    public List<Block> blocks(Long userId) {
        var p=data.professional(userId,false);
        return data.blocks(p.getId(),clock.instant()).stream().map(this::block).toList();
    }

    @Transactional
    public Block addBlock(Long userId, BlockInput input) {
        var p=data.professional(userId,true);
        if(!input.startsAt().isBefore(input.endsAt())) throw new IllegalArgumentException("La fecha final debe ser posterior al inicio.");
        var startsAt=input.startsAt().atZone(BUSINESS_ZONE).toInstant();
        var endsAt=input.endsAt().atZone(BUSINESS_ZONE).toInstant();
        if(endsAt.isBefore(clock.instant())) throw new IllegalArgumentException("No puedes bloquear una fecha pasada.");
        var block=new ScheduleBlock(); block.setSpecialist(p); block.setStartsAt(startsAt);
        block.setEndsAt(endsAt); block.setReason(input.reason().trim()); data.save(block); return block(block);
    }

    @Transactional
    public void removeBlock(Long userId, Long id) {
        var p=data.professional(userId,true); var block=data.find(ScheduleBlock.class,id);
        if(!block.getSpecialist().getId().equals(p.getId())) throw new ResourceNotFoundException("Bloqueo no encontrado.");
        data.remove(block);
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

    public Summary summary(Long userId) {
        var professional=data.professional(userId,false);
        var all=data.allAppointments(professional.getId());
        var now=clock.instant();
        var next=all.stream().filter(b->b.getStartsAt().isAfter(now) && b.getStatus()==BookingStatus.CONFIRMADA)
                .min(Comparator.comparing(SessionBooking::getStartsAt)).map(this::receipt).orElse(null);
        return new Summary(all.size(),count(all,BookingStatus.PENDIENTE),count(all,BookingStatus.CONFIRMADA),
                count(all,BookingStatus.COMPLETADA),count(all,BookingStatus.REPROGRAMACION_PROPUESTA),next);
    }

    public List<Contact> contacts(Long userId) {
        var p=data.professional(userId,false); var result=new LinkedHashMap<String,Contact>();
        var bookings=data.allAppointments(p.getId());
        var byCustomer=new LinkedHashMap<Long,List<SessionBooking>>();
        for(var booking:bookings) byCustomer.computeIfAbsent(booking.getCustomer().getId(),ignored->new ArrayList<>()).add(booking);
        for(var entry:byCustomer.entrySet()) {
            var customer=entry.getValue().getFirst().getCustomer();
            result.put("CLIENT-"+customer.getId(),contactFromCustomer(customer,entry.getValue()));
        }
        for(var manual:data.manualContacts(p.getId())) result.put("MANUAL-"+manual.getId(),contactFromManual(manual));
        return result.values().stream().sorted(Comparator.comparing(Contact::displayName,String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Transactional
    public Contact addContact(Long userId, ContactInput input) {
        var p=data.professional(userId,true); var contact=new ProfessionalContact(); contact.setSpecialist(p);
        contact.setDisplayName(input.displayName().trim()); contact.setEmail(blankToNull(input.email()));
        contact.setPhone(blankToNull(input.phone())); contact.setNotes(blankToNull(input.notes())); data.save(contact);
        return contactFromManual(contact);
    }

    public ContactDetail contact(Long userId,String key) {
        var p=data.professional(userId,false);
        if(key.startsWith("CLIENT-")) {
            Long customerId=parseKey(key,"CLIENT-");
            var history=data.forProfessionalCustomer(p.getId(),customerId);
            if(history.isEmpty()) throw new ResourceNotFoundException("Contacto no encontrado.");
            return new ContactDetail(contactFromCustomer(history.getFirst().getCustomer(),history),history.stream().map(this::receipt).toList());
        }
        Long id=parseKey(key,"MANUAL-"); var manual=data.find(ProfessionalContact.class,id);
        if(!manual.getSpecialist().getId().equals(p.getId())) throw new ResourceNotFoundException("Contacto no encontrado.");
        var history=manual.getCustomer()==null?List.<SessionBooking>of():data.forProfessionalCustomer(p.getId(),manual.getCustomer().getId());
        return new ContactDetail(contactFromManual(manual),history.stream().map(this::receipt).toList());
    }

    @Transactional
    public void removeContact(Long userId,Long id) {
        var p=data.professional(userId,true); var contact=data.find(ProfessionalContact.class,id);
        if(!contact.getSpecialist().getId().equals(p.getId())) throw new ResourceNotFoundException("Contacto no encontrado.");
        data.remove(contact);
    }

    @Transactional
    public Receipt bookContact(Long userId, Long customerId, ContactAppointmentInput input) {
        var professional=data.professional(userId,false);
        if(data.forProfessionalCustomer(professional.getId(),customerId).isEmpty())
            throw new ResourceNotFoundException("Contacto no encontrado.");
        var customer=data.customerById(customerId);
        professional=data.professional(userId,true);
        var session=service(input.serviceId());
        if(!session.getSpecialist().getId().equals(professional.getId()))
            throw new ResourceNotFoundException("Sesión no encontrada.");
        var date=input.startsAt().atZone(BUSINESS_ZONE).toLocalDate();
        var slot=slots(session,date,input.modality(),input.locationId(),customer.getId(),null).slots().stream()
                .filter(item->item.startsAt().equals(input.startsAt())).findFirst()
                .orElseThrow(()->new BookingConflictException("Este horario ya no está disponible. Selecciona otro."));
        var booking=new SessionBooking();
        booking.setCode("CITA-"+UUID.randomUUID().toString().replace("-","").substring(0,24));
        booking.setCustomer(customer); booking.setService(session); booking.setSpecialist(professional);
        booking.setStartsAt(slot.startsAt()); booking.setEndsAt(slot.endsAt()); booking.setModality(input.modality());
        booking.setLocation(input.locationId()==null?null:data.find(Location.class,input.locationId()));
        booking.setVirtualUrl(input.modality()==SessionModality.ONLINE
                ? virtualUrlForSlot(professional.getId(),date,input.startsAt()) : null);
        booking.setPrice(session.getPrice()); booking.setCurrency(session.getCurrency());
        booking.setStatus(BookingStatus.CONFIRMADA); booking.setCustomerNotes(input.reason().trim());
        booking.setExpiresAt(null); data.save(booking); return receipt(booking);
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
        boolean pending = booking.getStatus() == BookingStatus.PENDIENTE || booking.getStatus() == BookingStatus.REPROGRAMACION_PROPUESTA;
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
        booking.setVirtualUrl(input.modality()==SessionModality.ONLINE ? virtualUrlForSlot(specialistId,date,input.startsAt()) : null);
        booking.setStatus(BookingStatus.PENDIENTE);
        booking.setExpiresAt(now.plusSeconds(holdMinutes * 60L));
        booking.setRescheduledAt(now);
        booking.setRescheduleReason(input.reason().trim());
        booking.setRescheduledBy("CLIENTE");
        booking.setCancelledAt(null);
        booking.setCancellationReason(null);
        booking.setCancelledBy(null);
        return receipt(booking);
    }

    @Transactional
    public Receipt proposeReschedule(Long userId,Long id,Reschedule input) {
        var p=data.professional(userId,true); var booking=data.find(SessionBooking.class,id); var now=clock.instant();
        if(!booking.getSpecialist().getId().equals(p.getId()) || booking.getOrderItem()!=null)
            throw new ResourceNotFoundException("Solicitud no encontrada.");
        if((booking.getStatus()!=BookingStatus.PENDIENTE && booking.getStatus()!=BookingStatus.CONFIRMADA)
                || !booking.getStartsAt().isAfter(now)) throw new BookingConflictException("Solo puedes proponer cambios en citas futuras y vigentes.");
        var s=service(booking.getService().getId()); var date=input.startsAt().atZone(BUSINESS_ZONE).toLocalDate();
        var slot=slots(s,date,input.modality(),input.locationId(),booking.getCustomer().getId(),booking.getId()).slots().stream()
                .filter(item->item.startsAt().equals(input.startsAt())).findFirst()
                .orElseThrow(()->new BookingConflictException("Este horario ya no está disponible. Selecciona otro."));
        booking.setPreviousStartsAt(booking.getStartsAt()); booking.setPreviousEndsAt(booking.getEndsAt());
        booking.setStartsAt(slot.startsAt()); booking.setEndsAt(slot.endsAt()); booking.setModality(input.modality());
        booking.setLocation(input.locationId()==null?null:data.find(Location.class,input.locationId()));
        booking.setVirtualUrl(input.modality()==SessionModality.ONLINE ? virtualUrlForSlot(p.getId(),date,input.startsAt()) : null);
        booking.setStatus(BookingStatus.REPROGRAMACION_PROPUESTA); booking.setExpiresAt(now.plusSeconds(holdMinutes*60L));
        booking.setRescheduledAt(now); booking.setRescheduleReason(input.reason().trim()); booking.setRescheduledBy("PROFESIONAL");
        return receipt(booking);
    }

    @Transactional
    public Receipt respondProposal(Long userId,Long id,ProposalDecision input) {
        var specialistId=data.specialistForCustomerAppointment(userId,id); data.lock(data.find(SpecialistProfile.class,specialistId));
        var booking=data.find(SessionBooking.class,id); var now=clock.instant();
        if(booking.getStatus()!=BookingStatus.REPROGRAMACION_PROPUESTA || (booking.getExpiresAt()!=null && !booking.getExpiresAt().isAfter(now)))
            throw new BookingConflictException("La propuesta ya no está disponible.");
        if(input.accepted()) { booking.setStatus(BookingStatus.CONFIRMADA); booking.setExpiresAt(null); }
        else { cancel(booking,now,input.reason()==null||input.reason().isBlank()?"Propuesta rechazada por el cliente":input.reason(),"CLIENTE"); }
        return receipt(booking);
    }

    private void cancel(SessionBooking booking, Instant now, String reason, String actor) {
        boolean pending = booking.getStatus() == BookingStatus.PENDIENTE || booking.getStatus() == BookingStatus.REPROGRAMACION_PROPUESTA;
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
    private Rule rule(ProfessionalAvailability a) { return new Rule(a.getId(),a.getDayOfWeek(),a.getStartTime(),a.getEndTime(),a.getValidFrom(),a.getValidUntil(),venue(a.getLocation()),a.getBreakMinutes(),a.getVirtualUrl()); }
    private Block block(ScheduleBlock b) { return new Block(b.getId(),b.getStartsAt(),b.getEndsAt(),b.getReason()); }
    private Venue venue(Location l) { return l==null?null:new Venue(l.getId(),l.getName(),l.getAddress()); }
    private long count(List<SessionBooking> bookings,BookingStatus status) { return bookings.stream().filter(b->b.getStatus()==status).count(); }
    private String virtualUrlForSlot(Long professionalId,LocalDate date,Instant startsAt) {
        var localTime=startsAt.atZone(BUSINESS_ZONE).toLocalTime();
        return data.rules(professionalId).stream().filter(a->a.getLocation()==null && a.getDayOfWeek()==date.getDayOfWeek().getValue()
                        && !a.getValidFrom().isAfter(date) && (a.getValidUntil()==null || !a.getValidUntil().isBefore(date))
                        && !localTime.isBefore(a.getStartTime()) && localTime.isBefore(a.getEndTime()))
                .map(ProfessionalAvailability::getVirtualUrl).filter(Objects::nonNull).findFirst().orElse(null);
    }
    private String normalizeVirtualUrl(String value) {
        String url=blankToNull(value); if(url==null) return null;
        try {
            var uri=java.net.URI.create(url);
            if(!"https".equalsIgnoreCase(uri.getScheme()) || !"meet.google.com".equalsIgnoreCase(uri.getHost()))
                throw new IllegalArgumentException("Usa un enlace válido de Google Meet (https://meet.google.com/...).");
            return uri.toString();
        } catch(IllegalArgumentException ex) {
            if(ex.getMessage()!=null && ex.getMessage().startsWith("Usa un enlace")) throw ex;
            throw new IllegalArgumentException("Usa un enlace válido de Google Meet (https://meet.google.com/...).");
        }
    }
    private String blankToNull(String value) { return value==null||value.isBlank()?null:value.trim(); }
    private Long parseKey(String key,String prefix) {
        if(key==null || !key.startsWith(prefix)) throw new ResourceNotFoundException("Contacto no encontrado.");
        try { return Long.valueOf(key.substring(prefix.length())); }
        catch(NumberFormatException ex) { throw new ResourceNotFoundException("Contacto no encontrado."); }
    }
    private Contact contactFromCustomer(com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile customer,List<SessionBooking> history) {
        var now=clock.instant();
        var last=history.stream().map(SessionBooking::getStartsAt).filter(i->i.isBefore(now)).max(Comparator.naturalOrder()).orElse(null);
        var next=history.stream().filter(b->b.getStartsAt().isAfter(now) && b.getStatus()!=BookingStatus.CANCELADA)
                .map(SessionBooking::getStartsAt).min(Comparator.naturalOrder()).orElse(null);
        return new Contact("CLIENT-"+customer.getId(),"CLIENTE",customer.getId(),customer.getId(),
                customer.getFirstNames()+" "+customer.getLastNames(),customer.getUser().getEmail(),customer.getPhone(),null,history.size(),last,next);
    }
    private Contact contactFromManual(ProfessionalContact contact) {
        return new Contact("MANUAL-"+contact.getId(),"MANUAL",contact.getId(),contact.getCustomer()==null?null:contact.getCustomer().getId(),
                contact.getDisplayName(),contact.getEmail(),contact.getPhone(),contact.getNotes(),0,null,null);
    }
    private Receipt receipt(SessionBooking b) {
        String status=b.getStatus().name();
        if(b.getStatus()==BookingStatus.PENDIENTE && b.getExpiresAt()!=null && !b.getExpiresAt().isAfter(clock.instant())) status="EXPIRADA";
        String visibleVirtualUrl=(b.getStatus()==BookingStatus.CONFIRMADA || b.getStatus()==BookingStatus.COMPLETADA)
                ? b.getVirtualUrl() : null;
        return new Receipt(b.getId(),b.getCode(),b.getService().getId(),b.getService().getTitle(),b.getSpecialist().getPublicName(),
            b.getCustomer().getFirstNames()+" "+b.getCustomer().getLastNames(),b.getStartsAt(),b.getEndsAt(),b.getModality(),venue(b.getLocation()),
            b.getPrice(),b.getCurrency(),status,b.getExpiresAt(),b.getCustomerNotes(),b.getCancelledAt(),
            b.getCancellationReason(),b.getCancelledBy(),b.getRescheduledAt(),b.getPreviousStartsAt(),
            b.getPreviousEndsAt(),b.getRescheduleReason(),b.getRescheduledBy(),visibleVirtualUrl);
    }
}

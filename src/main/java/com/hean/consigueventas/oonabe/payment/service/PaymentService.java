package com.hean.consigueventas.oonabe.payment.service;

import com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus;
import com.hean.consigueventas.oonabe.common.enums.PurchaseItemType;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.Event;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.event.repository.EventRepository;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import com.hean.consigueventas.oonabe.payment.domain.DigitalReceiptStatus;
import com.hean.consigueventas.oonabe.payment.domain.PaymentStatus;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import com.hean.consigueventas.oonabe.payment.entity.DigitalReceipt;
import com.hean.consigueventas.oonabe.payment.entity.Payment;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrderItem;
import com.hean.consigueventas.oonabe.payment.dto.*;
import com.hean.consigueventas.oonabe.payment.repository.DigitalReceiptRepository;
import com.hean.consigueventas.oonabe.payment.repository.PaymentRepository;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderItemRepository;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.user.repository.TemporaryCustomerProfileRepository;
import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.booking.exception.BookingConflictException;
import com.hean.consigueventas.oonabe.booking.entity.EventAttendee;
import com.hean.consigueventas.oonabe.booking.repository.EventBookingRepository;
import com.hean.consigueventas.oonabe.booking.repository.EventAttendeeRepository;
import com.hean.consigueventas.oonabe.booking.repository.BookingStatusHistoryRepository;
import com.hean.consigueventas.oonabe.booking.entity.BookingStatusHistory;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.common.enums.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PaymentRepository paymentRepository;
    private final DigitalReceiptRepository digitalReceiptRepository;
    private final TemporaryCustomerProfileRepository customerProfileRepository;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final EventRepository eventRepository;
    private final OneToOneServiceRepository oneToOneServiceRepository;
    private final EventBookingRepository eventBookingRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    private final BookingStatusHistoryRepository bookingStatusHistoryRepository;

    @Transactional
    public CheckoutResponseDto createOrder(Long customerUserId, CheckoutRequestDto checkoutRequest) {
        CustomerProfile customer = customerProfileRepository.findByUserId(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de cliente no encontrado para el usuario: " + customerUserId));

        PurchaseOrder order = new PurchaseOrder();
        order.setCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomer(customer);
        order.setStatus(PurchaseOrderStatus.ABIERTA);
        order.setSubtotal(BigDecimal.ZERO);
        order.setTotalDiscount(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCurrency("EUR");
        order.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));

        order = purchaseOrderRepository.save(order);

        List<CheckoutResponseItemDto> responseItems = new ArrayList<>();
        BigDecimal totalSubtotal = BigDecimal.ZERO;

        // Hold occurrence locks in a deterministic order for the entire checkout.
        checkoutRequest.getItems().stream()
                .filter(i -> i.getItemType() == PurchaseItemType.EVENTO)
                .map(CheckoutItemDto::getReferenceId).distinct().sorted()
                .forEach(id -> eventOccurrenceRepository.findLockedById(id));

        for (CheckoutItemDto itemDto : checkoutRequest.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setOrder(order);
            item.setItemType(itemDto.getItemType());
            item.setReferenceId(itemDto.getReferenceId());
            item.setQuantity(itemDto.getQuantity());

            if (itemDto.getItemType() == PurchaseItemType.EVENTO) {
                // Primero intentamos buscar como EventOccurrence (para reservas)
                EventOccurrence occurrence = eventOccurrenceRepository.findById(itemDto.getReferenceId()).orElse(null);
                if (occurrence != null) {
                    Event event = occurrence.getEvent();
                    item.setUnitPrice(event.getPriceFrom() != null ? event.getPriceFrom() : BigDecimal.ZERO);
                    item.setDescription("Entrada para evento: " + event.getTitle());
                    order.setCurrency(event.getCurrency() != null ? event.getCurrency() : "EUR");
                } else {
                    // Fallback directo a Event
                    Event event = eventRepository.findById(itemDto.getReferenceId())
                            .orElseThrow(() -> new ResourceNotFoundException("No se encontró el evento u ocurrencia con ID: " + itemDto.getReferenceId()));
                    item.setUnitPrice(event.getPriceFrom() != null ? event.getPriceFrom() : BigDecimal.ZERO);
                    item.setDescription("Entrada para evento: " + event.getTitle());
                    order.setCurrency(event.getCurrency() != null ? event.getCurrency() : "EUR");
                }
            } else if (itemDto.getItemType() == PurchaseItemType.SESION) {
                OneToOneService service = oneToOneServiceRepository.findById(itemDto.getReferenceId())
                        .orElseThrow(() -> new ResourceNotFoundException("No se encontró la sesión 1:1 con ID: " + itemDto.getReferenceId()));
                item.setUnitPrice(service.getPrice() != null ? service.getPrice() : BigDecimal.ZERO);
                item.setDescription("Sesión 1:1: " + service.getTitle());
                order.setCurrency(service.getCurrency() != null ? service.getCurrency() : "EUR");
            } else {
                throw new BusinessLogicException("Tipo de ítem no válido: " + itemDto.getItemType());
            }

            item.setTotalAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            item = purchaseOrderItemRepository.save(item);

            if (item.getItemType() == PurchaseItemType.EVENTO) {
                EventOccurrence occurrence = eventOccurrenceRepository.findById(item.getReferenceId()).orElse(null);
                if (occurrence != null) {
                    if (occurrence.getStatus() != EventOccurrenceStatus.PROGRAMADA
                            || !occurrence.getStartsAt().isAfter(Instant.now())) {
                        throw new BusinessLogicException("Esta fecha ya no está disponible para reservar.");
                    }
                    if (occurrence.getReservedSpots() + item.getQuantity() > occurrence.getCapacity()) {
                        throw new BusinessLogicException("No hay suficientes plazas disponibles para la sesión elegida.");
                    }
                    occurrence.setReservedSpots(occurrence.getReservedSpots() + item.getQuantity());
                    eventOccurrenceRepository.save(occurrence);

                    EventBooking booking = new EventBooking();
                    booking.setCode("BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    booking.setOrderItem(item);
                    booking.setCustomer(customer);
                    booking.setOccurrence(occurrence);
                    booking.setQuantity(item.getQuantity());
                    booking.setUnitPrice(item.getUnitPrice());
                    booking.setTotalAmount(item.getTotalAmount());
                    booking.setCurrency(order.getCurrency());
                    booking.setStatus(BookingStatus.PENDIENTE);
                    booking.setCreatedAt(Instant.now());
                    eventBookingRepository.save(booking);

                    if (itemDto.getAttendees() != null && !itemDto.getAttendees().isEmpty()) {
                        for (com.hean.consigueventas.oonabe.payment.dto.AttendeeDto attendeeDto : itemDto.getAttendees()) {
                            EventAttendee attendee = new EventAttendee();
                            attendee.setEventBooking(booking);
                            attendee.setAttendeeName(attendeeDto.getName() + " " + (attendeeDto.getLastName() != null ? attendeeDto.getLastName() : ""));
                            attendee.setAttendeeEmail(attendeeDto.getEmail());
                            attendee.setAttendanceStatus(AttendanceStatus.PENDIENTE);
                            attendee.setRegisteredBy(customer.getUser());
                            attendee.setRegisteredAt(Instant.now());
                            eventAttendeeRepository.save(attendee);
                        }
                    } else {
                        EventAttendee attendee = new EventAttendee();
                        attendee.setEventBooking(booking);
                        attendee.setAttendeeName(customer.getFirstNames() + " " + customer.getLastNames());
                        attendee.setAttendeeEmail(customer.getUser().getEmail());
                        attendee.setAttendeePhone(customer.getPhone());
                        attendee.setAttendanceStatus(AttendanceStatus.PENDIENTE);
                        attendee.setRegisteredBy(customer.getUser());
                        attendee.setRegisteredAt(Instant.now());
                        eventAttendeeRepository.save(attendee);
                    }
                }
            }

            totalSubtotal = totalSubtotal.add(item.getTotalAmount());

            responseItems.add(new CheckoutResponseItemDto(
                    item.getReferenceId(),
                    item.getItemType(),
                    item.getDescription(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalAmount()
            ));
        }

        order.setSubtotal(totalSubtotal);
        order.setTotalAmount(totalSubtotal.subtract(order.getTotalDiscount()));
        order = purchaseOrderRepository.save(order);

        CheckoutResponseDto response = new CheckoutResponseDto();
        response.setCode(order.getCode());
        response.setSubtotal(order.getSubtotal());
        response.setTotalDiscount(order.getTotalDiscount());
        response.setTotalAmount(order.getTotalAmount());
        response.setCurrency(order.getCurrency());
        response.setStatus(order.getStatus());
        response.setItems(responseItems);

        return response;
    }

    @Transactional
    public PaymentResponseDto processPayment(String orderCode, PaymentProcessRequestDto request) {
        PurchaseOrder order = purchaseOrderRepository.findLockedByCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la orden con código: " + orderCode));

        List<EventBooking> orderBookings = eventBookingRepository.findLockedByOrderId(order.getId());
        orderBookings.stream().map(b -> b.getOccurrence().getId()).distinct().sorted()
                .forEach(id -> eventOccurrenceRepository.findLockedById(id));
        if (order.getStatus() == PurchaseOrderStatus.CANCELADA
                && orderBookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.CANCELADA)) {
            throw new BookingConflictException("El profesional rechazó la reserva y la orden fue cancelada.");
        }
        Set<Long> rejectedItemIds = orderBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELADA && b.getOrderItem() != null)
                .map(b -> b.getOrderItem().getId())
                .collect(Collectors.toSet());

        if (order.getStatus() != PurchaseOrderStatus.ABIERTA) {
            throw new BusinessLogicException("La orden de compra ya no está abierta. Estado actual: " + order.getStatus());
        }

        if (order.getExpiresAt() != null && order.getExpiresAt().isBefore(Instant.now())) {
            order.setStatus(PurchaseOrderStatus.EXPIRADA);
            purchaseOrderRepository.save(order);
            throw new BusinessLogicException("La orden de compra ha expirado.");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setProvider(request.getMethod().toUpperCase());
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());

        boolean approved = false;
        String errorMessage = null;

        if (request.getMethod().equalsIgnoreCase("WHATSAPP")) {
            throw new BusinessLogicException("El flujo de WhatsApp no requiere procesamiento de pago en el backend. Use el endpoint correspondiente.");
        } else if (request.getMethod().equalsIgnoreCase("YAPE")) {
            String phone = request.getYapePhoneNumber();
            String otp = request.getYapeOtp();
            if (phone == null || phone.replaceAll("[^0-9]", "").length() != 9 || otp == null || otp.replaceAll("[^0-9]", "").length() != 6) {
                errorMessage = "Simulación: Celular Yape debe tener 9 dígitos numéricos y el OTP debe tener 6 dígitos numéricos.";
            } else {
                approved = true;
            }
        } else if (request.getMethod().equalsIgnoreCase("TARJETA")) {
            String card = request.getCardNumber();
            if (card == null || card.replaceAll("[^0-9]", "").length() < 13 || request.getCardCvv() == null || request.getCardExpiration() == null) {
                errorMessage = "Simulación: Datos de tarjeta de crédito/débito incompletos o incorrectos.";
            } else if (card.replaceAll("[^0-9]", "").endsWith("4444")) {
                errorMessage = "Simulación: Transacción rechazada por fondos insuficientes o rechazo bancario.";
            } else {
                approved = true;
            }
        } else {
            throw new BusinessLogicException("Método de pago no soportado: " + request.getMethod());
        }

        if (approved) {
            payment.setStatus(PaymentStatus.APROBADO);
            payment.setExternalReference("TX-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
            payment.setProcessedAt(Instant.now());
            payment = paymentRepository.save(payment);

            order.setStatus(PurchaseOrderStatus.PAGADA);
            order.setPaidAt(Instant.now());
            purchaseOrderRepository.save(order);

            // Generar el comprobante de pago digital
            DigitalReceipt receipt = new DigitalReceipt();
            receipt.setPayment(payment);
            receipt.setNumber("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            receipt.setCustomerName(order.getCustomer().getFirstNames() + " " + order.getCustomer().getLastNames());
            receipt.setCustomerEmail(order.getCustomer().getUser().getEmail());

            // Rejected bookings were already removed from the order total; they are neither confirmed nor billed.
            List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByOrderIdOrderByIdAsc(order.getId()).stream()
                    .filter(i -> !rejectedItemIds.contains(i.getId()))
                    .toList();

            for (EventBooking booking : orderBookings) {
                if (booking.getStatus() == BookingStatus.CANCELADA) continue;
                booking.setStatus(BookingStatus.CONFIRMADA);
                eventBookingRepository.save(booking);
            }

            StringBuilder descBuilder = new StringBuilder("Compra en Oona:\n");
            for (PurchaseOrderItem item : items) {
                descBuilder.append("- ").append(item.getQuantity()).append("x ").append(item.getDescription()).append("\n");
            }
            receipt.setPurchaseDescription(descBuilder.toString().trim());
            receipt.setAmount(order.getTotalAmount());
            receipt.setCurrency(order.getCurrency());
            receipt.setStatus(DigitalReceiptStatus.VALIDO);
            receipt.setIssuedAt(Instant.now());

            digitalReceiptRepository.save(receipt);

            return new PaymentResponseDto("APROBADO", payment.getExternalReference(), null);
        } else {
            payment.setStatus(PaymentStatus.RECHAZADO);
            payment.setErrorMessage(errorMessage);
            paymentRepository.save(payment);

            return new PaymentResponseDto("RECHAZADO", null, errorMessage);
        }
    }

    @Transactional(readOnly = true)
    public WhatsAppRedirectDto generateWhatsAppLink(String orderCode) {
        PurchaseOrder order = purchaseOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la orden con código: " + orderCode));

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findAll().stream()
                .filter(i -> i.getOrder().getId().equals(order.getId()))
                .toList();

        if (items.isEmpty()) {
            throw new BusinessLogicException("La orden de compra no contiene ítems.");
        }

        PurchaseOrderItem firstItem = items.getFirst();
        SpecialistProfile specialist = null;

        if (firstItem.getItemType() == PurchaseItemType.EVENTO) {
            EventOccurrence occurrence = eventOccurrenceRepository.findById(firstItem.getReferenceId()).orElse(null);
            if (occurrence != null) {
                specialist = occurrence.getEvent().getSpecialist();
            } else {
                Event event = eventRepository.findById(firstItem.getReferenceId()).orElse(null);
                if (event != null) {
                    specialist = event.getSpecialist();
                }
            }
        } else if (firstItem.getItemType() == PurchaseItemType.SESION) {
            OneToOneService service = oneToOneServiceRepository.findById(firstItem.getReferenceId()).orElse(null);
            if (service != null) {
                specialist = service.getSpecialist();
            }
        }

        String phone = "+51999999999"; // Fallback por defecto si no hay número registrado
        if (specialist != null && specialist.getWhatsappPhone() != null && !specialist.getWhatsappPhone().isBlank()) {
            phone = specialist.getWhatsappPhone();
        }

        // Limpiar el teléfono para que solo tenga dígitos para wa.me/
        String cleanPhone = phone.replaceAll("[^0-9]", "");

        String message = "Hola, me interesa adquirir entradas para " + firstItem.getDescription() + 
                " en Oona. El código de mi orden de compra es " + order.getCode() + ". ¿Cómo coordinamos el pago?";
        
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String redirectUrl = "https://wa.me/" + cleanPhone + "?text=" + encodedMessage;

        return new WhatsAppRedirectDto(redirectUrl);
    }

    @Transactional(readOnly = true)
    public DigitalReceiptDto getDigitalReceipt(String orderCode) {
        DigitalReceipt receipt = digitalReceiptRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el recibo digital para la orden: " + orderCode));

        DigitalReceiptDto dto = new DigitalReceiptDto();
        dto.setNumber(receipt.getNumber());
        dto.setCustomerName(receipt.getCustomerName());
        dto.setCustomerEmail(receipt.getCustomerEmail());
        dto.setPurchaseDescription(receipt.getPurchaseDescription());
        dto.setAmount(receipt.getAmount());
        dto.setCurrency(receipt.getCurrency());
        dto.setIssuedAt(receipt.getIssuedAt());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<MyBookingResponseDto> getMyBookings(Long userId) {
        List<EventBooking> bookings = eventBookingRepository.findByCustomer_User_IdOrderByCreatedAtDesc(userId);
        List<Long> cancelledIds = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELADA)
                .map(EventBooking::getId)
                .toList();
        // Latest cancellation entry per booking: who cancelled it and why.
        Map<Long, BookingStatusHistory> cancellations = cancelledIds.isEmpty() ? Map.of()
                : bookingStatusHistoryRepository.findByBookingTypeAndNewStatusAndBookingIdIn(
                        PurchaseItemType.EVENTO, BookingStatus.CANCELADA.name(), cancelledIds).stream()
                .collect(Collectors.toMap(BookingStatusHistory::getBookingId, h -> h,
                        BinaryOperator.maxBy(Comparator.comparing(BookingStatusHistory::getCreatedAt))));
        return bookings.stream()
                .map(b -> toMyBookingResponse(b, cancellations.get(b.getId())))
                .toList();
    }

    private MyBookingResponseDto toMyBookingResponse(EventBooking booking, BookingStatusHistory cancellation) {
        EventOccurrence occurrence = booking.getOccurrence();
        Event event = occurrence.getEvent();

        MyBookingResponseDto dto = new MyBookingResponseDto();
        dto.setCode(booking.getCode());
        dto.setStatus(booking.getStatus());
        dto.setQuantity(booking.getQuantity());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setCurrency(booking.getCurrency());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setEventId(event.getId());
        dto.setEventTitle(event.getTitle());
        dto.setOccurrenceStartsAt(occurrence.getStartsAt());
        dto.setModality(event.getModality());
        if (occurrence.getLocation() != null) {
            dto.setLocationName(occurrence.getLocation().getName());
            dto.setCityName(occurrence.getLocation().getCity() != null
                    ? occurrence.getLocation().getCity().getName()
                    : null);
        }
        if (cancellation != null) {
            dto.setCancelledAt(booking.getCancelledAt() != null ? booking.getCancelledAt() : cancellation.getCreatedAt());
            // The release job records no user; a professional rejection always does.
            dto.setCancellationCause(cancellation.getChangedBy() == null ? "EXPIRADA" : "RECHAZADA");
            dto.setCancellationReason(cancellation.getReason());
        }
        return dto;
    }
}

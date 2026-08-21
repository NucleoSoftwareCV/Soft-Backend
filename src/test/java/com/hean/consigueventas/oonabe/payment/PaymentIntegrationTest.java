package com.hean.consigueventas.oonabe.payment;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.common.enums.PurchaseItemType;
import com.hean.consigueventas.oonabe.common.exception.ResourceNotFoundException;
import com.hean.consigueventas.oonabe.event.entity.EventOccurrence;
import com.hean.consigueventas.oonabe.event.repository.EventOccurrenceRepository;
import com.hean.consigueventas.oonabe.oneToOneSession.entity.OneToOneService;
import com.hean.consigueventas.oonabe.oneToOneSession.repository.OneToOneServiceRepository;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import com.hean.consigueventas.oonabe.payment.dto.CheckoutItemDto;
import com.hean.consigueventas.oonabe.payment.dto.CheckoutRequestDto;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import com.hean.consigueventas.oonabe.payment.repository.PurchaseOrderRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.CustomerProfile;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.TemporaryCustomerProfileRepository;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest {

    private static final String BASE_URL = "/api/v1/payments/checkout";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemporaryCustomerProfileRepository customerProfileRepository;

    @Autowired
    private EventOccurrenceRepository eventOccurrenceRepository;

    @Autowired
    private OneToOneServiceRepository oneToOneServiceRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetailsImpl testUser;

    @BeforeEach
    void setUp() {
        User user = userRepository.findByUsername("user1")
                .orElseThrow(() -> new ResourceNotFoundException("Test user 'user1' not found in database."));
        testUser = UserDetailsImpl.build(user);

        // Asegurarse de que exista el CustomerProfile para user1
        if (customerProfileRepository.findByUserId(user.getId()).isEmpty()) {
            CustomerProfile profile = new CustomerProfile();
            profile.setUser(user);
            profile.setFirstNames("User");
            profile.setLastNames("One");
            profile.setPhone("+51999999999");
            customerProfileRepository.saveAndFlush(profile);
        }
    }

    @Test
    void unauthenticatedUserCannotCheckout() throws Exception {
        CheckoutRequestDto request = new CheckoutRequestDto();
        mockMvc.perform(post(BASE_URL + "/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCanCheckoutAndPaySuccessfully() throws Exception {
        // 1. Obtener ítems reales de la base de datos
        List<EventOccurrence> occurrences = eventOccurrenceRepository.findAll();
        List<OneToOneService> services = oneToOneServiceRepository.findAll();

        org.assertj.core.api.Assertions.assertThat(occurrences).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(services).isNotEmpty();

        java.time.Instant startsAt = java.time.Instant.now().plus(java.time.Duration.ofDays(1));
        EventOccurrence occurrence = occurrences.get(0);
        occurrence.setStatus(com.hean.consigueventas.oonabe.common.enums.EventOccurrenceStatus.PROGRAMADA);
        occurrence.setStartsAt(startsAt);
        occurrence.setEndsAt(startsAt.plus(java.time.Duration.ofHours(2)));
        occurrence.setReservedSpots(0);
        occurrence.setCapacity(Math.max(occurrence.getCapacity(), 10));
        eventOccurrenceRepository.saveAndFlush(occurrence);
        OneToOneService service = services.get(0);

        // 2. Armar el request de Checkout
        CheckoutRequestDto checkoutRequest = new CheckoutRequestDto();
        CheckoutItemDto item1 = new CheckoutItemDto();
        item1.setItemType(PurchaseItemType.EVENTO);
        item1.setReferenceId(occurrence.getId());
        item1.setQuantity((short) 2);

        CheckoutItemDto item2 = new CheckoutItemDto();
        item2.setItemType(PurchaseItemType.SESION);
        item2.setReferenceId(service.getId());
        item2.setQuantity((short) 1);

        checkoutRequest.setItems(List.of(item1, item2));
        checkoutRequest.setBillingName("Test Billing Name");
        checkoutRequest.setBillingAddress("Test Billing Address 123");

        // 3. Ejecutar Checkout
        MvcResult checkoutResult = mockMvc.perform(post(BASE_URL + "/orders")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.status").value(PurchaseOrderStatus.ABIERTA.name()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn();

        String responseBody = checkoutResult.getResponse().getContentAsString();
        String orderCode = objectMapper.readTree(responseBody).get("code").asText();

        // 4. Pagar orden simulando Tarjeta exitosa
        String cardPaymentPayload = """
                {
                    "method": "TARJETA",
                    "cardNumber": "1234567812345678",
                    "cardHolder": "Test User",
                    "cardExpiration": "12/28",
                    "cardCvv": "123"
                }
                """;

        mockMvc.perform(post(BASE_URL + "/orders/" + orderCode + "/pay")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardPaymentPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROBADO"))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.errorMessage").isEmpty());

        // Verificar en BD que la orden cambió a PAGADA
        PurchaseOrder order = purchaseOrderRepository.findByCode(orderCode).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.PAGADA);
        org.assertj.core.api.Assertions.assertThat(order.getPaidAt()).isNotNull();

        // 5. Descargar comprobante digital
        mockMvc.perform(get(BASE_URL + "/receipts/" + orderCode)
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.customerName").value("User One"))
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.currency").value(order.getCurrency()));
    }

    @Test
    void paymentWithRejectedCardReturnsError() throws Exception {
        List<OneToOneService> services = oneToOneServiceRepository.findAll();
        OneToOneService service = services.get(0);

        CheckoutRequestDto checkoutRequest = new CheckoutRequestDto();
        CheckoutItemDto item = new CheckoutItemDto();
        item.setItemType(PurchaseItemType.SESION);
        item.setReferenceId(service.getId());
        item.setQuantity((short) 1);
        checkoutRequest.setItems(List.of(item));

        MvcResult checkoutResult = mockMvc.perform(post(BASE_URL + "/orders")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String orderCode = objectMapper.readTree(checkoutResult.getResponse().getContentAsString()).get("code").asText();

        // Pagar orden con tarjeta que termina en 4444 (Simulación de rechazo)
        String cardPaymentPayload = """
                {
                    "method": "TARJETA",
                    "cardNumber": "1234567812344444",
                    "cardHolder": "Test User",
                    "cardExpiration": "12/28",
                    "cardCvv": "123"
                }
                """;

        mockMvc.perform(post(BASE_URL + "/orders/" + orderCode + "/pay")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardPaymentPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECHAZADO"))
                .andExpect(jsonPath("$.transactionId").isEmpty())
                .andExpect(jsonPath("$.errorMessage").exists());

        // La orden debe seguir ABIERTA para reintentar
        PurchaseOrder order = purchaseOrderRepository.findByCode(orderCode).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.ABIERTA);
    }

    @Test
    void getWhatsAppRedirectUrlSuccessfully() throws Exception {
        List<OneToOneService> services = oneToOneServiceRepository.findAll();
        OneToOneService service = services.get(0);

        CheckoutRequestDto checkoutRequest = new CheckoutRequestDto();
        CheckoutItemDto item = new CheckoutItemDto();
        item.setItemType(PurchaseItemType.SESION);
        item.setReferenceId(service.getId());
        item.setQuantity((short) 1);
        checkoutRequest.setItems(List.of(item));

        MvcResult checkoutResult = mockMvc.perform(post(BASE_URL + "/orders")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String orderCode = objectMapper.readTree(checkoutResult.getResponse().getContentAsString()).get("code").asText();

        // Obtener redirección de WhatsApp
        mockMvc.perform(get(BASE_URL + "/orders/" + orderCode + "/whatsapp")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl").value(org.hamcrest.Matchers.containsString("https://wa.me/")));
    }
}

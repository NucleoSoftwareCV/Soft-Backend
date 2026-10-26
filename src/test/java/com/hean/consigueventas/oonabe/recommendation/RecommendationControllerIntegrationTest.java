package com.hean.consigueventas.oonabe.recommendation;

import com.hean.consigueventas.oonabe.common.enums.EventModality;
import com.hean.consigueventas.oonabe.event.dto.request.EventFilterRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.recommendation.dto.response.RecommendedEventResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:recommendation_api;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationControllerIntegrationTest {

    private static final String PATH =
            "/api/v1/recommendations/events";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventRecommendationQueryService queryService;

    @Test
    @WithMockUser(username = "cliente", roles = "USER")
    void shouldReturnRecommendationsForAuthenticatedUser() throws Exception {
        PageRequest pageable = PageRequest.of(0, 12);

        when(queryService.getRecommendations(
                eq("cliente"),
                eq(pageable),
                any(EventFilterRequest.class),
                eq(true)
        )).thenReturn(new PageImpl<>(
                List.of(response()),
                pageable,
                1
        ));

        mockMvc.perform(get(PATH)
                        .param("username", "otra-cuenta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].event.id").value(7))
                .andExpect(jsonPath("$.content[0].event.title").value("Yoga"))
                .andExpect(jsonPath("$.content[0].occurrenceId").value(50))
                .andExpect(jsonPath("$.content[0].score").value(40))
                .andExpect(jsonPath("$.content[0].reason")
                        .value("De un profesional que sigues"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(12))
                .andExpect(header().string(
                        "Cache-Control",
                        containsString("no-store")
                ))
                .andExpect(header().string(
                        "Cache-Control",
                        containsString("private")
                ));

        verify(queryService).getRecommendations(
                eq("cliente"),
                eq(pageable),
                any(EventFilterRequest.class),
                eq(true)
        );

        verifyNoMoreInteractions(queryService);
    }

    @Test
    @WithMockUser(username = "cliente", roles = "USER")
    void shouldReturnEmptyPage() throws Exception {
        PageRequest pageable = PageRequest.of(1, 5);

        when(queryService.getRecommendations(
                eq("cliente"),
                eq(pageable),
                any(EventFilterRequest.class),
                eq(true)
        )).thenReturn(Page.empty(pageable));

        mockMvc.perform(get(PATH)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void shouldRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get(PATH))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(queryService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldRejectAccountsWithoutUserRole() throws Exception {
        mockMvc.perform(get(PATH))
                .andExpect(status().isForbidden());

        verifyNoInteractions(queryService);
    }

    @ParameterizedTest
    @CsvSource({
            "-1,12",
            "0,0",
            "0,101",
            "0,abc"
    })
    @WithMockUser(username = "cliente", roles = "USER")
    void shouldRejectInvalidPagination(
            String page,
            String size
    ) throws Exception {
        mockMvc.perform(get(PATH)
                        .param("page", page)
                        .param("size", size))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(queryService);
    }

    @Test
    @WithMockUser(username = "cliente", roles = "USER")
    void shouldRejectExternalSort() throws Exception {
        mockMvc.perform(get(PATH)
                        .param("sort", "title,asc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(queryService);
    }

    @Test
    @WithMockUser(username = "cliente", roles = "USER")
    void shouldForwardFiltersAndCityPolicy() throws Exception {
        PageRequest pageable = PageRequest.of(0, 12);

        when(queryService.getRecommendations(
                eq("cliente"),
                eq(pageable),
                any(EventFilterRequest.class),
                eq(false)
        )).thenReturn(Page.empty(pageable));

        mockMvc.perform(get(PATH)
                        .param("useProfileCity", "false")
                        .param("cityName", "Valencia")
                        .param("includeOnline", "true")
                        .param("categoryId", "10")
                        .param("search", "yoga")
                        .param("maxPrice", "25")
                        .param("dateFrom", "2026-11-01")
                        .param("dateTo", "2026-11-30"))
                .andExpect(status().isOk());

        ArgumentCaptor<EventFilterRequest> captor =
                ArgumentCaptor.forClass(EventFilterRequest.class);

        verify(queryService).getRecommendations(
                eq("cliente"),
                eq(pageable),
                captor.capture(),
                eq(false)
        );

        EventFilterRequest filter = captor.getValue();

        assertThat(filter.cityName()).isEqualTo("Valencia");
        assertThat(filter.includeOnline()).isTrue();
        assertThat(filter.categoryId()).isEqualTo(10L);
        assertThat(filter.search()).isEqualTo("yoga");
        assertThat(filter.maxPrice()).isEqualByComparingTo("25");
        assertThat(filter.dateFrom())
                .isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(filter.dateTo())
                .isEqualTo(LocalDate.of(2026, 11, 30));
    }

    @ParameterizedTest
    @CsvSource({
            "dateFrom,2026-11-30,dateTo,2026-11-01",
            "hourFrom,18,hourTo,10",
            "minPrice,50,maxPrice,10",
            "categoryId,10,categoryIds,20",
            "hourFrom,24,hourTo,24"
    })
    @WithMockUser(username = "cliente", roles = "USER")
    void shouldRejectInvalidFilters(
            String firstName,
            String firstValue,
            String secondName,
            String secondValue
    ) throws Exception {
        mockMvc.perform(get(PATH)
                        .param(firstName, firstValue)
                        .param(secondName, secondValue))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(queryService);
    }

    private RecommendedEventResponse response() {
        EventCardResponse card = new EventCardResponse(
                7L,
                "Yoga",
                null,
                EventModality.ONLINE,
                null,
                "EUR",
                10L,
                "Yoga",
                30L,
                "Ana",
                null,
                null,
                Instant.parse("2026-11-01T12:00:00Z"),
                Instant.parse("2026-11-01T13:00:00Z"),
                null,
                null,
                null,
                null,
                false
        );

        return new RecommendedEventResponse(
                card,
                50L,
                40,
                "De un profesional que sigues"
        );
    }
}
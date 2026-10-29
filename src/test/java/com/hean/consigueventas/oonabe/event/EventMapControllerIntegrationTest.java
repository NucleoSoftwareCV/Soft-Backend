package com.hean.consigueventas.oonabe.event;

import com.hean.consigueventas.oonabe.event.repository.EventMapRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventMapControllerIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean EventMapRepository repository;

    @Test
    void allowsAnonymousAccessAndUsesBoundedPagination() throws Exception {
        when(repository.findAvailableLocations(any(Instant.class), eq(PageRequest.of(0, 100))))
                .thenReturn(Page.empty(PageRequest.of(0, 100)));
        mvc.perform(get("/api/v1/events/map"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.size").value(100));
        verify(repository).findAvailableLocations(any(Instant.class), eq(PageRequest.of(0, 100)));
    }

    @ParameterizedTest
    @CsvSource({"-1,100", "0,0", "0,101"})
    void rejectsInvalidPagination(int page, int size) throws Exception {
        mvc.perform(get("/api/v1/events/map")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}

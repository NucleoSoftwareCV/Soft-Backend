package com.hean.consigueventas.oonabe.travel;
import com.hean.consigueventas.oonabe.profileCliente.service.ClientProfileService;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ReferenceLocationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ClientTravelControllerIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoBean ClientProfileService profiles;
    @MockitoBean EventTravelService travel;
    static final String BASE="/api/v1/client-profiles/me";
    @Test void anonymousCannotReadPrivateOrigin() throws Exception {
        mvc.perform(get(BASE+"/reference-location")).andExpect(status().isUnauthorized());
        verifyNoInteractions(profiles);
    }
    @Test @WithMockUser(username="professional",roles="PROFESSIONAL")
    void wrongRoleCannotReadPrivateOrigin() throws Exception {
        mvc.perform(get(BASE+"/reference-location")).andExpect(status().isForbidden());
        verifyNoInteractions(profiles);
    }
    @Test @WithMockUser(username="ana",roles="USER")
    void retrievesOnlyAuthenticatedUserAndDisablesCaching() throws Exception {
        when(profiles.getReferenceLocation("ana")).thenReturn(new ReferenceLocationResponse("Barrio",40.0,-3.0,true));
        mvc.perform(get(BASE+"/reference-location")).andExpect(status().isOk())
            .andExpect(header().string("Cache-Control",containsString("no-store")))
            .andExpect(jsonPath("$.label").value("Barrio"));
        verify(profiles).getReferenceLocation("ana");
    }
    @Test @WithMockUser(username="ana",roles="USER")
    void rejectsInvalidCoordinatesAndSessionIds() throws Exception {
        mvc.perform(put(BASE+"/reference-location").contentType("application/json")
            .content("{\"label\":\"Barrio\",\"latitude\":91,\"longitude\":0,\"approximate\":true}")).andExpect(status().isBadRequest());
        mvc.perform(post(BASE+"/event-travel").contentType("application/json")
            .content("{\"occurrenceIds\":[-1]}")).andExpect(status().isBadRequest());
        verifyNoInteractions(profiles,travel);
    }
}

package com.hean.consigueventas.oonabe.professionalApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hean.consigueventas.oonabe.auth.dto.request.TokenRefreshRequest;
import com.hean.consigueventas.oonabe.auth.dto.response.TokenRefreshResponse;
import com.hean.consigueventas.oonabe.auth.entity.RefreshToken;
import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.auth.service.AuthService;
import com.hean.consigueventas.oonabe.auth.service.RefreshTokenService;
import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.masterdata.entity.City;
import com.hean.consigueventas.oonabe.masterdata.repository.CityRepository;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfilePartialUpdateRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.response.SpecialistProfileResponse;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
import com.hean.consigueventas.oonabe.profileProfesional.service.SpecialistProfileService;
import com.hean.consigueventas.oonabe.professionalApplication.dto.request.ProfessionalApplicationDecisionRequest;
import com.hean.consigueventas.oonabe.professionalApplication.dto.request.ProfessionalApplicationRequest;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalApplicationStatus;
import com.hean.consigueventas.oonabe.professionalApplication.enums.ProfessionalType;
import com.hean.consigueventas.oonabe.professionalApplication.repository.ProfessionalApplicationRepository;
import com.hean.consigueventas.oonabe.user.entity.Role;
import com.hean.consigueventas.oonabe.user.entity.User;
import com.hean.consigueventas.oonabe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfessionalApplicationIntegrationTest {

    private static final String MY_APPLICATION = "/api/v1/professional-applications/me";
    private static final String ADMIN_APPLICATIONS = "/api/v1/admin/professional-applications";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfessionalApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SpecialistProfileService specialistProfileService;

    @Autowired
    private SpecialistProfileRepository specialistProfileRepository;

    @Test
    void unauthenticatedUserCannotCreateOrReadApplication() throws Exception {
        mockMvc.perform(put(MY_APPLICATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get(MY_APPLICATION))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCreatesAndUpdatesSinglePendingApplication() throws Exception {
        saveApplication("user1", validRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.email").value("user1@oona.es"));

        ProfessionalApplicationRequest updated = new ProfessionalApplicationRequest(
                "Usuario Uno",
                activeCity().getId(),
                ProfessionalType.COACH,
                "+34600999888",
                "Acompaño procesos de bienestar y crecimiento personal.",
                true
        );
        saveApplication("user1", updated)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionalType").value("COACH"))
                .andExpect(jsonPath("$.whatsappPhone").value("+34600999888"));

        assertThat(applicationRepository.count()).isEqualTo(1);
    }

    @Test
    void requestValidatesPrivacyAndActiveCity() throws Exception {
        ProfessionalApplicationRequest withoutPrivacy = new ProfessionalApplicationRequest(
                "Usuario Uno",
                activeCity().getId(),
                ProfessionalType.YOGA,
                "+34600123456",
                "Tengo experiencia impartiendo clases de yoga.",
                false
        );
        saveApplication("user1", withoutPrivacy)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        ProfessionalApplicationRequest missingCity = new ProfessionalApplicationRequest(
                "Usuario Uno",
                Long.MAX_VALUE,
                ProfessionalType.YOGA,
                "+34600123456",
                "Tengo experiencia impartiendo clases de yoga.",
                true
        );
        saveApplication("user1", missingCity)
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectedApplicationCanBeCorrectedAndResubmitted() throws Exception {
        Long applicationId = createApplicationAndGetId("user1");
        decide(applicationId, ProfessionalApplicationStatus.RECHAZADO, "Falta información profesional")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECHAZADO"));

        saveApplication("user1", validRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.rejectionReason").doesNotExist());
    }

    @Test
    void adminListsAndApprovesApplicationAndRefreshReturnsProfessionalRole() throws Exception {
        Long applicationId = createApplicationAndGetId("user1");

        mockMvc.perform(get(ADMIN_APPLICATIONS)
                        .param("status", "PENDIENTE")
                        .param("size", "1")
                        .with(user(principal("admin_main1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        decide(applicationId, ProfessionalApplicationStatus.APROBADO, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROBADO"));

        User approvedUser = userRepository.findByUsername("user1").orElseThrow();
        assertThat(approvedUser.getRoles())
                .extracting(Role::getName)
                .contains(Role.ROLE_PROFESSIONAL);

        var automaticProfile = specialistProfileRepository
                .findByUserId(approvedUser.getId())
                .orElseThrow();
        assertThat(automaticProfile.getApprovalStatus()).isEqualTo(ApprovalStatus.APROBADO);
        assertThat(automaticProfile.getPublicationStatus()).isEqualTo(PublicationStatus.BORRADOR);
        assertThat(automaticProfile.getBiography()).isEmpty();
        assertThat(automaticProfile.getPhotoUrl()).isEmpty();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(approvedUser.getId());
        TokenRefreshResponse refreshed = authService.refreshToken(
                new TokenRefreshRequest(refreshToken.getToken())
        );
        assertThat(refreshed.roles()).contains("USER", "PROFESSIONAL");
    }

    @Test
    void regularUserCannotEvaluateApplications() throws Exception {
        Long applicationId = createApplicationAndGetId("user1");
        ProfessionalApplicationDecisionRequest request =
                new ProfessionalApplicationDecisionRequest(
                        ProfessionalApplicationStatus.APROBADO,
                        null
                );

        mockMvc.perform(patch(ADMIN_APPLICATIONS + "/{id}/decision", applicationId)
                        .with(user(principal("user2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void approvedProfessionalCreatesApprovedDraftProfileWithoutSecondReview() throws Exception {
        Long applicationId = createApplicationAndGetId("user1");
        decide(applicationId, ProfessionalApplicationStatus.APROBADO, null)
                .andExpect(status().isOk());

        SpecialistProfileRequest profileRequest = new SpecialistProfileRequest(
                "Usuario Uno Bienestar",
                "PROFESIONALES",
                "Profesional de bienestar integral.",
                "Acompaño procesos individuales mediante yoga y respiración consciente.",
                "+34600123456",
                null,
                "user1@oona.es",
                null,
                Set.of(),
                Set.of()
        );

        SpecialistProfileResponse updated =
                specialistProfileService.updateMyProfile("user1", profileRequest);
        assertThat(updated.approvalStatus()).isEqualTo(ApprovalStatus.APROBADO);
        assertThat(updated.publicationStatus()).isEqualTo(PublicationStatus.BORRADOR);
    }

    @Test
    void incompleteDraftCanBeEditedAndPublishExplainsEveryMissingRequirement() throws Exception {
        Long applicationId = createApplicationAndGetId("user1");
        decide(applicationId, ProfessionalApplicationStatus.APROBADO, null)
                .andExpect(status().isOk());

        SpecialistProfilePartialUpdateRequest draftUpdate =
                new SpecialistProfilePartialUpdateRequest(
                        "Usuario Uno Actualizado",
                        null,
                        "",
                        "",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        SpecialistProfileResponse updated =
                specialistProfileService.updateMyProfilePartial("user1", draftUpdate);

        assertThat(updated.publicName()).isEqualTo("Usuario Uno Actualizado");
        assertThat(updated.biography()).isEmpty();
        assertThat(updated.description()).isEmpty();
        assertThat(updated.publicationStatus()).isEqualTo(PublicationStatus.BORRADOR);

        assertThatThrownBy(() -> specialistProfileService.publishMyProfile("user1"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("foto de perfil")
                .hasMessageContaining("banner")
                .hasMessageContaining("biografía")
                .hasMessageContaining("descripción");

        mockMvc.perform(patch("/api/v1/specialist-profiles/me/publish")
                        .with(user("user1").roles("PROFESSIONAL")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "No se puede publicar el perfil. Completa: foto de perfil, banner, biografía, descripción."
                ));

        mockMvc.perform(patch("/api/v1/specialist-profiles/me")
                        .with(user("user1").roles("PROFESSIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"whatsappPhone\":\"+51 92803719599\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("whatsappPhone"));
    }

    @Test
    void editingPublishedProfileKeepsItVisible() {
        SpecialistProfilePartialUpdateRequest request =
                new SpecialistProfilePartialUpdateRequest(
                        null,
                        null,
                        "Biografia actualizada sin ocultar el perfil.",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        SpecialistProfileResponse updated =
                specialistProfileService.updateMyProfilePartial(
                        "professional_demo",
                        request
                );

        assertThat(updated.publicationStatus())
                .isEqualTo(PublicationStatus.PUBLICADO);
        assertThat(updated.biography())
                .isEqualTo("Biografia actualizada sin ocultar el perfil.");
    }

    private org.springframework.test.web.servlet.ResultActions saveApplication(
            String username,
            ProfessionalApplicationRequest request) throws Exception {
        return mockMvc.perform(put(MY_APPLICATION)
                .with(user(principal(username)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private org.springframework.test.web.servlet.ResultActions decide(
            Long applicationId,
            ProfessionalApplicationStatus status,
            String rejectionReason) throws Exception {
        ProfessionalApplicationDecisionRequest request =
                new ProfessionalApplicationDecisionRequest(status, rejectionReason);
        return mockMvc.perform(patch(ADMIN_APPLICATIONS + "/{id}/decision", applicationId)
                .with(user(principal("admin_main1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private Long createApplicationAndGetId(String username) throws Exception {
        saveApplication(username, validRequest()).andExpect(status().isOk());
        return applicationRepository.findByUserId(
                userRepository.findByUsername(username).orElseThrow().getId()
        ).orElseThrow().getId();
    }

    private ProfessionalApplicationRequest validRequest() {
        return new ProfessionalApplicationRequest(
                "Usuario Uno",
                activeCity().getId(),
                ProfessionalType.YOGA,
                "+34600123456",
                "Tengo experiencia impartiendo clases de yoga y bienestar.",
                true
        );
    }

    private City activeCity() {
        return cityRepository.findByIsActiveTrue().stream().findFirst().orElseThrow();
    }

    private UserDetailsImpl principal(String username) {
        return UserDetailsImpl.build(userRepository.findByUsername(username).orElseThrow());
    }
}

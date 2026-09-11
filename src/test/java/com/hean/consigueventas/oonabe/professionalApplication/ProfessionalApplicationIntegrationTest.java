
package com.hean.consigueventas.oonabe.professionalApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hean.consigueventas.oonabe.auth.entity.RefreshToken;
import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.auth.service.AuthService;
import com.hean.consigueventas.oonabe.auth.service.RefreshTokenService;
import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.common.exception.BusinessLogicException;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfilePartialUpdateRequest;
import com.hean.consigueventas.oonabe.profileProfesional.dto.request.SpecialistProfileRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfessionalApplicationIntegrationTest {

    private static final String APPLICATIONS =
            "/api/v1/professional-applications";

    private static final String ADMIN_APPLICATIONS =
            "/api/v1/admin/professional-applications";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfessionalApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SpecialistProfileService specialistProfileService;

    @Autowired
    private SpecialistProfileRepository specialistProfileRepository;

    @Test
    void unauthenticatedUserCannotCreateApplication() throws Exception {
        mockMvc.perform(
                post(APPLICATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        validRequest()
                                )
                        )
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCanCreateApplication() throws Exception {
        mockMvc.perform(
                post(APPLICATIONS)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        validRequest()
                                )
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.fullName").value("Usuario Uno"))
                .andExpect(jsonPath("$.city").value("Lima"))
                .andExpect(jsonPath("$.email").value("user1@oona.es"));
    }

    @Test
    void publicApplicationRequiresNameCityAndEmail() throws Exception {
        ProfessionalApplicationRequest invalidRequest =
               new ProfessionalApplicationRequest(
        "",
        "",
        "correo-invalido",
        "Terapeuta",
        "+51 999 999 999",
        "Quiero formar parte de Círculo Oona."
);

        mockMvc.perform(
                post(APPLICATIONS)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        invalidRequest
                                )
                        )
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void pendingApplicationCannotBeDuplicatedForSameUser()
            throws Exception {

        mockMvc.perform(
                post(APPLICATIONS)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        validRequest()
                                )
                        )
        )
                .andExpect(status().isOk());

        mockMvc.perform(
                post(APPLICATIONS)
                        .with(user(principal("user1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        validRequest()
                                )
                        )
        )
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanListPendingApplications() throws Exception {
        createApplicationAndGetId();

        mockMvc.perform(
                get(ADMIN_APPLICATIONS)
                        .param("status", "PENDIENTE")
                        .param("size", "10")
                        .with(
                                user(
                                        principal("admin_main1")
                                )
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fullName")
                        .value("Usuario Uno"))
                .andExpect(jsonPath("$.content[0].city")
                        .value("Lima"))
                .andExpect(jsonPath("$.content[0].email")
                        .value("user1@oona.es"));
    }

    @Test
    void regularUserCannotEvaluateApplications()
            throws Exception {

        Long applicationId =
                createApplicationAndGetId();

        ProfessionalApplicationDecisionRequest request =
                new ProfessionalApplicationDecisionRequest(
                        ProfessionalApplicationStatus.APROBADO,
                        null
                );

        mockMvc.perform(
                patch(
                        ADMIN_APPLICATIONS
                                + "/{id}/decision",
                        applicationId
                )
                        .with(
                                user(
                                        principal("user2")
                                )
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanApproveApplicationAndPromoteExistingUser()
            throws Exception {

        Long applicationId =
                createApplicationAndGetId();

        decide(
                applicationId,
                ProfessionalApplicationStatus.APROBADO,
                null
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("APROBADO"))
                .andExpect(jsonPath("$.city")
                        .value("Lima"))
                .andExpect(jsonPath("$.userId")
                        .value(userId("user1")));

        User approvedUser =
                userRepository
                        .findByUsername("user1")
                        .orElseThrow();

        assertThat(approvedUser.getRoles())
                .extracting(Role::getName)
                .contains(Role.ROLE_PROFESSIONAL);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        approvedUser.getId()
                );

        var refreshed =
                authService.refreshToken(
                        new com.hean.consigueventas.oonabe.auth.dto.request.TokenRefreshRequest(
                                refreshToken.getToken()
                        )
                );

        assertThat(refreshed.roles())
                .contains(
                        "USER",
                        "PROFESSIONAL"
                );
    }

    @Test
    void adminCanRejectApplicationWithReason()
            throws Exception {

        Long applicationId =
                createApplicationAndGetId();

        decide(
                applicationId,
                ProfessionalApplicationStatus.RECHAZADO,
                "Falta información profesional"
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("RECHAZADO"))
                .andExpect(jsonPath("$.rejectionReason")
                        .value(
                                "Falta información profesional"
                        ));
    }

    @Test
    void rejectionRequiresReason()
            throws Exception {

        Long applicationId =
                createApplicationAndGetId();

        decide(
                applicationId,
                ProfessionalApplicationStatus.RECHAZADO,
                null
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void approvedProfessionalCanUpdateProfile()
            throws Exception {

        Long applicationId =
                createApplicationAndGetId();

        decide(
                applicationId,
                ProfessionalApplicationStatus.APROBADO,
                null
        )
                .andExpect(status().isOk());

        SpecialistProfileRequest profileRequest =
                new SpecialistProfileRequest(
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
                specialistProfileService.updateMyProfile(
                        "user1",
                        profileRequest
                );

        assertThat(updated.approvalStatus())
                .isEqualTo(
                        ApprovalStatus.APROBADO
                );

        assertThat(updated.publicationStatus())
                .isEqualTo(
                        PublicationStatus.BORRADOR
                );
    }

    @Test
    void incompleteDraftCanBeEditedAndPublishExplainsEveryMissingRequirement()
            throws Exception {

        Long applicationId =
                createApplicationAndGetId();

        decide(
                applicationId,
                ProfessionalApplicationStatus.APROBADO,
                null
        )
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
                specialistProfileService.updateMyProfilePartial(
                        "user1",
                        draftUpdate
                );

        assertThat(updated.publicName())
                .isEqualTo(
                        "Usuario Uno Actualizado"
                );

        assertThat(updated.biography())
                .isEmpty();

        assertThat(updated.description())
                .isEmpty();

        assertThat(updated.publicationStatus())
                .isEqualTo(
                        PublicationStatus.BORRADOR
                );

        assertThatThrownBy(
                () ->
                        specialistProfileService
                                .publishMyProfile("user1")
        )
                .isInstanceOf(
                        BusinessLogicException.class
                )
                .hasMessageContaining(
                        "foto de perfil"
                )
                .hasMessageContaining(
                        "banner"
                )
                .hasMessageContaining(
                        "biografía"
                )
                .hasMessageContaining(
                        "descripción"
                );

        mockMvc.perform(
                patch(
                        "/api/v1/specialist-profiles/me/publish"
                )
                        .with(
                                user("user1")
                                        .roles("PROFESSIONAL")
                        )
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No se puede publicar el perfil. Completa: foto de perfil, banner, biografía, descripción."
                                )
                );

        mockMvc.perform(
                patch(
                        "/api/v1/specialist-profiles/me"
                )
                        .with(
                                user("user1")
                                        .roles("PROFESSIONAL")
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                "{\"whatsappPhone\":\"+51 92803719599\"}"
                        )
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.errors[0].field"
                        )
                                .value("whatsappPhone")
                );
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
                .isEqualTo(
                        PublicationStatus.PUBLICADO
                );

        assertThat(updated.biography())
                .isEqualTo(
                        "Biografia actualizada sin ocultar el perfil."
                );
    }

    private Long createApplicationAndGetId()
            throws Exception {

        mockMvc.perform(
                post(APPLICATIONS)
                        .with(user(principal("user1")))
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        validRequest()
                                )
                        )
        )
                .andExpect(status().isOk());

        return applicationRepository
                .findByUserId(userId("user1"))
                .orElseThrow()
                .getId();
    }

  private ProfessionalApplicationRequest validRequest() {
    return new ProfessionalApplicationRequest(
            "Usuario Uno",
            "Lima",
            "user1@oona.es",
            "Terapeuta",
            "+51 999 999 999",
            "Quiero formar parte de Círculo Oona para compartir mis servicios."
    );
}

    private org.springframework.test.web.servlet.ResultActions decide(
            Long applicationId,
            ProfessionalApplicationStatus status,
            String rejectionReason
    ) throws Exception {

        ProfessionalApplicationDecisionRequest request =
                new ProfessionalApplicationDecisionRequest(
                        status,
                        rejectionReason
                );

        return mockMvc.perform(
                patch(
                        ADMIN_APPLICATIONS
                                + "/{id}/decision",
                        applicationId
                )
                        .with(
                                user(
                                        principal("admin_main1")
                                )
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        );
    }

    private Long userId(String username) {

        return userRepository
                .findByUsername(username)
                .orElseThrow()
                .getId();
    }

    private UserDetailsImpl principal(
            String username
    ) {

        return UserDetailsImpl.build(
                userRepository
                        .findByUsername(username)
                        .orElseThrow()
        );
    }
}

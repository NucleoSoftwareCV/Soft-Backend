package com.hean.consigueventas.oonabe.interaction;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.common.enums.ApprovalStatus;
import com.hean.consigueventas.oonabe.common.enums.PublicationStatus;
import com.hean.consigueventas.oonabe.interaction.entity.ProfessionalFollow;
import com.hean.consigueventas.oonabe.interaction.repository.ProfessionalFollowRepository;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import com.hean.consigueventas.oonabe.profileCliente.repository.ClientProfileRepository;
import com.hean.consigueventas.oonabe.profileProfesional.entity.SpecialistProfile;
import com.hean.consigueventas.oonabe.profileProfesional.repository.SpecialistProfileRepository;
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

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfessionalFollowingIntegrationTest {

    private static final String BASE_PATH = "/api/v1/professional-follows";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfessionalFollowRepository followRepository;

    @Autowired
    private SpecialistProfileRepository specialistProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientProfileRepository clientProfileRepository;

    @Test
    void followIsAuthenticatedAndIdempotent() throws Exception {
        SpecialistProfile professional = profileOwnedBy("specialist_ana");

        follow(professional.getId(), "user1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionalId").value(professional.getId()))
                .andExpect(jsonPath("$.following").value(true));

        follow(professional.getId(), "user1").andExpect(status().isOk());

        assertThat(followRepository.count()).isEqualTo(1);
    }

    @Test
    void unfollowIsIdempotent() throws Exception {
        SpecialistProfile professional = profileOwnedBy("specialist_ana");
        follow(professional.getId(), "user1").andExpect(status().isOk());

        mockMvc.perform(delete(BASE_PATH + "/{professionalId}", professional.getId())
                        .with(user(principal("user1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));

        mockMvc.perform(delete(BASE_PATH + "/{professionalId}", professional.getId())
                        .with(user(principal("user1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));

        assertThat(followRepository.count()).isZero();
    }

    @Test
    void statusReflectsCurrentRelationship() throws Exception {
        SpecialistProfile professional = profileOwnedBy("specialist_ana");

        getFollowStatus(professional.getId(), "user1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));

        follow(professional.getId(), "user1").andExpect(status().isOk());

        getFollowStatus(professional.getId(), "user1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true));
    }

    @Test
    void listIsPagedAndContainsOnlyMinimalPublicCard() throws Exception {
        SpecialistProfile ana = profileOwnedBy("specialist_ana");
        SpecialistProfile carlos = profileOwnedBy("specialist_carlos");
        User follower = userRepository.findByUsername("user1").orElseThrow();
        saveFollow(follower, ana, Instant.parse("2026-07-15T10:00:00Z"));
        saveFollow(follower, carlos, Instant.parse("2026-07-16T10:00:00Z"));

        mockMvc.perform(get(BASE_PATH).param("size", "1").with(user(principal("user1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content[0].id").value(carlos.getId()))
                .andExpect(jsonPath("$.content[0].slug").exists())
                .andExpect(jsonPath("$.content[0].publicName").exists())
                .andExpect(jsonPath("$.content[0].profileCategory").exists())
                .andExpect(jsonPath("$.content[0].photoUrl").exists())
                .andExpect(jsonPath("$.content[0].followedAt").exists())
                .andExpect(jsonPath("$.content[0].biography").doesNotExist())
                .andExpect(jsonPath("$.content[0].whatsappPhone").doesNotExist())
                .andExpect(jsonPath("$.content[0].userId").doesNotExist());
    }

    @Test
    void hiddenProfileKeepsRelationshipButIsExcludedFromList() throws Exception {
        SpecialistProfile professional = profileOwnedBy("specialist_ana");
        follow(professional.getId(), "user1").andExpect(status().isOk());

        professional.setPublicationStatus(PublicationStatus.BORRADOR);
        specialistProfileRepository.saveAndFlush(professional);

        mockMvc.perform(get(BASE_PATH).with(user(principal("user1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        assertThat(followRepository.count()).isEqualTo(1);
    }

    @Test
    void pendingRejectedDraftOrMissingProfileCannotBeFollowed() throws Exception {
        SpecialistProfile professional = profileOwnedBy("specialist_ana");
        professional.setApprovalStatus(ApprovalStatus.PENDIENTE);
        specialistProfileRepository.saveAndFlush(professional);

        follow(professional.getId(), "user1")
                .andExpect(status().isNotFound());

        professional.setApprovalStatus(ApprovalStatus.RECHAZADO);
        specialistProfileRepository.saveAndFlush(professional);
        follow(professional.getId(), "user1")
                .andExpect(status().isNotFound());

        professional.setApprovalStatus(ApprovalStatus.APROBADO);
        professional.setPublicationStatus(PublicationStatus.BORRADOR);
        specialistProfileRepository.saveAndFlush(professional);
        follow(professional.getId(), "user1")
                .andExpect(status().isNotFound());

        follow(Long.MAX_VALUE, "user1")
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotFollowOwnProfessionalProfile() throws Exception {
        SpecialistProfile ownProfile = profileOwnedBy("specialist_ana");

        follow(ownProfile.getId(), "specialist_ana")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.oona.local/errors/business-rule"));
    }

    @Test
    void unauthenticatedRequestReturnsUnauthorized() throws Exception {
        SpecialistProfile professional = profileOwnedBy("specialist_ana");

        mockMvc.perform(put(BASE_PATH + "/{professionalId}", professional.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private org.springframework.test.web.servlet.ResultActions follow(Long professionalId, String username) throws Exception {
        return mockMvc.perform(put(BASE_PATH + "/{professionalId}", professionalId)
                .with(user(principal(username))));
    }

    private org.springframework.test.web.servlet.ResultActions getFollowStatus(Long professionalId, String username) throws Exception {
        return mockMvc.perform(get(BASE_PATH + "/{professionalId}", professionalId)
                .with(user(principal(username))));
    }

    private SpecialistProfile profileOwnedBy(String username) {
        User owner = userRepository.findByUsername(username).orElseThrow();
        return specialistProfileRepository.findByUserId(owner.getId()).orElseThrow();
    }

    private UserDetailsImpl principal(String username) {
        return UserDetailsImpl.build(userRepository.findByUsername(username).orElseThrow());
    }

    private void saveFollow(
            User follower,
            SpecialistProfile professional,
            Instant followedAt
    ) {
        ClientProfile clientProfile =
                clientProfileRepository.findByUserId(follower.getId())
                        .orElseGet(() -> {
                            ClientProfile newClientProfile = new ClientProfile();

                            newClientProfile.setUser(follower);
                            newClientProfile.setCommunicationEmail(follower.getEmail());

                            return clientProfileRepository.saveAndFlush(newClientProfile);
                        });

        ProfessionalFollow follow = new ProfessionalFollow();

        follow.setClientProfile(clientProfile);
        follow.setSpecialistProfile(professional);
        follow.setFollowedAt(followedAt);

        followRepository.saveAndFlush(follow);
    }
}

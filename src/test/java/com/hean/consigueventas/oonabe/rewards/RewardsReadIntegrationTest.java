package com.hean.consigueventas.oonabe.rewards;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.experienceType.entity.ExperienceType;
import com.hean.consigueventas.oonabe.rewards.Entity.EarnPointsRules;
import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import com.hean.consigueventas.oonabe.rewards.Enum.TransactionType;
import com.hean.consigueventas.oonabe.rewards.Enum.UserPointStatus;
import com.hean.consigueventas.oonabe.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RewardsReadIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/rewards/points")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/rewards/transactions")).andExpect(status().isUnauthorized());
    }

    @Test
    void scopesBalanceAndHistoryToTheAuthenticatedAccount() throws Exception {
        User owner = createUser("loyalty-owner");
        User other = createUser("loyalty-other");
        User empty = createUser("loyalty-empty");
        ExperienceType experience = new ExperienceType();
        experience.setName("Rewards test");
        em.persist(experience);
        EarnPointsRules rule = EarnPointsRules.builder().experienceType(experience).reward(20).active(true).build();
        em.persist(rule);
        addMovement(owner, rule, 20);
        addMovement(other, rule, 80);
        em.flush();

        mvc.perform(get("/api/v1/rewards/points").param("userId", other.getId().toString())
                        .with(user(UserDetailsImpl.build(owner))))
                .andExpect(status().isOk()).andExpect(content().string("20"))
                .andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(get("/api/v1/rewards/transactions").with(user(UserDetailsImpl.build(owner))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pointsQuantity").value(20))
                .andExpect(jsonPath("$[0].userEmail").doesNotExist());
        mvc.perform(get("/api/v1/rewards/points").with(user(UserDetailsImpl.build(empty))))
                .andExpect(status().isOk()).andExpect(content().string("0"));
        mvc.perform(get("/api/v1/rewards/transactions").with(user(UserDetailsImpl.build(empty))))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    private User createUser(String username) {
        User account = new User();
        account.setUsername(username);
        account.setEmail(username + "@example.com");
        account.setPasswordHash("unused-in-test");
        em.persist(account);
        return account;
    }

    private void addMovement(User account, EarnPointsRules rule, int points) {
        LocalDateTime now = LocalDateTime.of(2026, 11, 20, 12, 0);
        UserPoints balance = UserPoints.builder().user(account).availablePoints(points)
                .status(UserPointStatus.ACTIVE).updatedAt(now).build();
        em.persist(balance);
        em.persist(PointsTransactions.builder().userPoints(balance).earnPointsRules(rule)
                .type(TransactionType.EARN).pointsQuantity(points).balanceAfter(points).createdAt(now).build());
    }
}

package com.hean.consigueventas.oonabe.rewards.Service;

import com.hean.consigueventas.oonabe.rewards.Dto.Request.UpdateUserPointsRequest;
import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import com.hean.consigueventas.oonabe.rewards.Enum.UserPointStatus;
import com.hean.consigueventas.oonabe.rewards.Repository.UserPointsRepository;
import com.hean.consigueventas.oonabe.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPointsService {

    private final UserPointsRepository userPointsRepository;
    private final FinderService finderService;

    public UserPoints getOrCreateUserPoints(User user) {
        return userPointsRepository.findByUserEmail(user.getEmail())
                .orElseGet(() -> {
                    UserPoints newUserPoints = UserPoints.builder()
                            .user(user)
                            .availablePoints(0)
                            .status(UserPointStatus.ACTIVE)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return userPointsRepository.save(newUserPoints);
                });
    }

    public Integer getAvailablePointsByUserEmail(String userEmail) {
        return userPointsRepository.findAvailablePointsByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Points of the user not found"));
    }

    public UserPoints findByUserEmail(String userEmail) {
        return userPointsRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("UserPoints not found"));
    }

    public UserPoints updateUserPoints(UpdateUserPointsRequest request) {
        Integer redeemPoints = request.getRedeemPoints();
        Long userId = request.getUserId();

        if (redeemPoints == null || redeemPoints <= 0) {
            throw new IllegalArgumentException("Redeem points must be greater than cero");
        }

        User user = finderService.findUserById(userId);

        UserPoints userPoints = userPointsRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("A UserPoints account not found"));

        Integer availablePoints = userPoints.getAvailablePoints();
        if (redeemPoints > availablePoints) {
            throw new RuntimeException("Redeem points cant be greater than availablePoints "
                    + availablePoints + redeemPoints);
        }
        userPoints.setAvailablePoints(availablePoints - redeemPoints);
        userPoints.setUpdatedAt(LocalDateTime.now());

        return userPointsRepository.save(userPoints);
    }

    /** Add points n returns the new total */
    public int addPoints(UserPoints userPoints, int points) {
        int newBalance = userPoints.getAvailablePoints() + points;
        userPoints.setAvailablePoints(newBalance);
        userPoints.setUpdatedAt(LocalDateTime.now());
        userPointsRepository.save(userPoints);
        return newBalance;
    }

    /** Subtract points n returns the new total */
    public int subtractPoints(UserPoints userPoints, int points) {
        int newBalance = userPoints.getAvailablePoints() - points;
        userPoints.setAvailablePoints(newBalance);
        userPoints.setUpdatedAt(LocalDateTime.now());
        userPointsRepository.save(userPoints);
        return newBalance;
    }
}

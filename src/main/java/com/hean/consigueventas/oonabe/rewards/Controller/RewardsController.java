package com.hean.consigueventas.oonabe.rewards.Controller;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.rewards.Dto.Request.EarnPointsRequest;
import com.hean.consigueventas.oonabe.rewards.Dto.Request.RedeemPointsRequest;
import com.hean.consigueventas.oonabe.rewards.Dto.Response.EarnPointsResponse;
import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import com.hean.consigueventas.oonabe.rewards.Service.PointsTransactionsService;
import com.hean.consigueventas.oonabe.rewards.Service.RewardsService;
import com.hean.consigueventas.oonabe.rewards.Service.UserPointsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rewards")
public class RewardsController {
    private final RewardsService rewardsService;
    private final UserPointsService userPointsService;
    private final PointsTransactionsService pointsTransactionsService;

    @GetMapping("/points")
    public ResponseEntity<Integer> getPointsByUserEmail(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        return ResponseEntity.ok(rewardsService.getPointsByUserEmail(userEmail));
    }

    /**Not available needs dtoResponse*/
    @GetMapping("/redemptions")
    public ResponseEntity<List<PointsTransactions>> getRedemptionsByUserEmail(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        return ResponseEntity.ok(rewardsService.getRedemptionsByUserEmail(userEmail));
    }

    @PostMapping("/earn")
    public ResponseEntity<EarnPointsResponse> earnRewardPoints(@Valid @RequestBody EarnPointsRequest request) {
        return ResponseEntity.ok(rewardsService.earnPoints(request));
    }

    @PostMapping("/redemption")
    public ResponseEntity<BigDecimal> getRedemptionByPoints(@Valid @RequestBody RedeemPointsRequest request) {
        return ResponseEntity.ok(rewardsService.getRedemptionByPoints(request));
    }

    @PostMapping("/redeem")
    public ResponseEntity<String> redeemPoints(@Valid @RequestBody RedeemPointsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rewardsService.redeemPoints(request));
    }
}

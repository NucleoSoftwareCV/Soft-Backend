package com.hean.consigueventas.oonabe.rewards.Controller;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.rewards.Dto.Response.PointsTransactionsResponse;
import com.hean.consigueventas.oonabe.rewards.Service.PointsTransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rewards/transactions")
public class PointsTransactionsController {
    private final PointsTransactionsService pointsTransactionsService;

    @GetMapping
    public ResponseEntity<List<PointsTransactionsResponse>> getTransactionsByUserEmail(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        return ResponseEntity.ok(pointsTransactionsService.getTransactionsByUserEmail(userEmail));
    }
}

package com.hean.consigueventas.oonabe.rewards.Controller;

import com.hean.consigueventas.oonabe.rewards.Dto.Request.UpdateUserPointsRequest;
import com.hean.consigueventas.oonabe.rewards.Entity.UserPoints;
import com.hean.consigueventas.oonabe.rewards.Service.UserPointsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rewards/acc")
public class UserPointsController {

    private final UserPointsService userPointsService;

    @PostMapping("/update")
    public ResponseEntity<UserPoints> updateUserPoints(@Valid @RequestBody UpdateUserPointsRequest request) {
        return ResponseEntity.ok(userPointsService.updateUserPoints(request));
    }
}

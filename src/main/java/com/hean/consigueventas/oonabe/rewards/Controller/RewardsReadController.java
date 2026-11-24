package com.hean.consigueventas.oonabe.rewards.Controller;

import com.hean.consigueventas.oonabe.auth.security.UserDetailsImpl;
import com.hean.consigueventas.oonabe.rewards.Dto.Response.PointTransactionItem;
import com.hean.consigueventas.oonabe.rewards.Repository.RewardsReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rewards")
public class RewardsReadController {
    private final RewardsReadRepository rewards;

    @GetMapping("/points")
    public ResponseEntity<Integer> balance(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(rewards.findBalance(user.getId()).orElse(0));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<PointTransactionItem>> history(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(rewards.findHistory(user.getId(), PageRequest.of(0, 50)));
    }
}

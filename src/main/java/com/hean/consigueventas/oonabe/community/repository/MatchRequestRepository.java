package com.hean.consigueventas.oonabe.community.repository;

import com.hean.consigueventas.oonabe.community.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
    Optional<MatchRequest> findByUserId(Long userId);
}

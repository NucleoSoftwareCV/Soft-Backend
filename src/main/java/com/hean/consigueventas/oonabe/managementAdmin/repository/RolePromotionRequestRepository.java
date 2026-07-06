package com.hean.consigueventas.oonabe.managementAdmin.repository;

import com.hean.consigueventas.oonabe.managementAdmin.entity.RolePromotionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolePromotionRequestRepository extends JpaRepository<RolePromotionRequest, Long> {
    Optional<RolePromotionRequest> findByUserIdAndStatus(Long userId, com.hean.consigueventas.oonabe.managementAdmin.enums.PromotionStatus status);
}

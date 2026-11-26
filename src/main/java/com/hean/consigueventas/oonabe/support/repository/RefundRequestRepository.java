package com.hean.consigueventas.oonabe.support.repository;

import com.hean.consigueventas.oonabe.support.entity.RefundRequest;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefundRequest r where r.id=:id")
    Optional<RefundRequest> lock(@Param("id") Long id);

    Optional<RefundRequest> findByTicketId(Long ticketId);

    List<RefundRequest> findByTransactionId(Long id);

    Page<RefundRequest> findByTicketUserId(Long id, Pageable pageable);
}

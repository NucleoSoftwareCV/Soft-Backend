package com.hean.consigueventas.oonabe.support.repository;

import com.hean.consigueventas.oonabe.support.entity.SupportTicket;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long>, JpaSpecificationExecutor<SupportTicket> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from SupportTicket t where t.id=:id")
    Optional<SupportTicket> lock(@Param("id") Long id);

    Page<SupportTicket> findByUserId(Long userId, Pageable pageable);
}

package com.hean.consigueventas.oonabe.support.repository;

import com.hean.consigueventas.oonabe.support.entity.SupportResponse;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface SupportResponseRepository extends JpaRepository<SupportResponse, Long> {
    List<SupportResponse> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}

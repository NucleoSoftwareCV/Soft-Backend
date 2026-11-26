package com.hean.consigueventas.oonabe.support.repository;

import com.hean.consigueventas.oonabe.support.entity.SupportAudit;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface SupportAuditRepository extends JpaRepository<SupportAudit, Long> {
    List<SupportAudit> findByTicketIdOrderByCreatedAtAscIdAsc(Long id);
}

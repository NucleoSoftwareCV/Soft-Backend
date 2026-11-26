package com.hean.consigueventas.oonabe.support.repository;

import com.hean.consigueventas.oonabe.support.entity.SupportAttachment;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface SupportAttachmentRepository extends JpaRepository<SupportAttachment, Long> {
    List<SupportAttachment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}

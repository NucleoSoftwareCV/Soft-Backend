package com.hean.consigueventas.oonabe.support.repository;

import com.hean.consigueventas.oonabe.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SupportPaymentRepository extends JpaRepository<Payment, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id=:id")
    Optional<Payment> lock(@Param("id") Long id);

    List<Payment> findByOrderCustomerUserIdOrderByCreatedAtDesc(Long id);
}

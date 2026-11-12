package com.hean.consigueventas.oonabe.payment.repository;

import com.hean.consigueventas.oonabe.payment.domain.PaymentStatus;
import com.hean.consigueventas.oonabe.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderIdInAndStatus(Collection<Long> orderIds, PaymentStatus status);
}

package com.hean.consigueventas.oonabe.payment.repository;

import com.hean.consigueventas.oonabe.payment.entity.DigitalReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DigitalReceiptRepository extends JpaRepository<DigitalReceipt, Long> {
    @Query("SELECT dr FROM DigitalReceipt dr WHERE dr.payment.order.code = :orderCode")
    Optional<DigitalReceipt> findByOrderCode(@Param("orderCode") String orderCode);
}

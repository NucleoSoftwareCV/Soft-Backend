package com.hean.consigueventas.oonabe.payment.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import com.hean.consigueventas.oonabe.payment.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PurchaseOrder p where p.code = :code")
    Optional<PurchaseOrder> findLockedByCode(@Param("code") String code);
    @Query("SELECT p FROM PurchaseOrder p JOIN FETCH p.customer c JOIN FETCH c.user WHERE p.code = :code")
    Optional<PurchaseOrder> findByCode(@Param("code") String code);
}

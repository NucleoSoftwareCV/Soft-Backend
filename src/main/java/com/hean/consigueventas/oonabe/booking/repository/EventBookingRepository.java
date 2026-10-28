package com.hean.consigueventas.oonabe.booking.repository;

import com.hean.consigueventas.oonabe.booking.entity.EventBooking;
import com.hean.consigueventas.oonabe.common.enums.BookingStatus;
import com.hean.consigueventas.oonabe.payment.domain.PurchaseOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long>, JpaSpecificationExecutor<EventBooking> {
    interface OwnedReference {
        Long getId();
        Long getOccurrenceId();
        String getOrderCode();
    }

    @Query("""
            select b.id as id, b.occurrence.id as occurrenceId, o.code as orderCode
            from EventBooking b left join b.orderItem i left join i.order o
            where b.code = :code and b.occurrence.event.specialist.user.id = :userId
            """)
    Optional<OwnedReference> findOwnedReference(@Param("code") String code, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"customer", "customer.user", "occurrence", "occurrence.event"})
    @Query("select b from EventBooking b where b.code = :code and b.occurrence.event.specialist.user.id = :userId")
    Optional<EventBooking> findOwnedByCode(@Param("code") String code, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from EventBooking b where b.code = :code and b.occurrence.event.specialist.user.id = :userId")
    Optional<EventBooking> findOwnedLockedByCode(@Param("code") String code, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from EventBooking b where b.orderItem.order.id = :orderId order by b.id")
    List<EventBooking> findLockedByOrderId(@Param("orderId") Long orderId);

    @Query("""
            select distinct o.code from EventBooking b join b.orderItem i join i.order o
            where o.status <> :paid and o.expiresAt < :now and b.status in :held
            """)
    List<String> findOrderCodesWithExpiredHolds(@Param("now") Instant now, @Param("paid") PurchaseOrderStatus paid,
            @Param("held") Collection<BookingStatus> held);

    @Override
    @EntityGraph(attributePaths = {"customer", "occurrence", "occurrence.event"})
    Page<EventBooking> findAll(Specification<EventBooking> spec, Pageable pageable);

    Optional<EventBooking> findByCode(String code);

    @EntityGraph(attributePaths = {"occurrence", "occurrence.event", "occurrence.location", "occurrence.location.city"})
    List<EventBooking> findByCustomer_User_IdOrderByCreatedAtDesc(Long userId);
}

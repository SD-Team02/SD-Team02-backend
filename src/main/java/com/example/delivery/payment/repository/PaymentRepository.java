package com.example.delivery.payment.repository;

import com.example.delivery.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query(value = "SELECT p FROM Payment p JOIN FETCH p.order o " +
            "WHERE o.userId = :userId " +
            "AND p.approvedAt BETWEEN :startDateTime AND :endDateTime",
            countQuery = "SELECT count(p) FROM Payment p JOIN p.order o " +
                    "WHERE o.userId = :userId " +
                    "AND p.approvedAt BETWEEN :startDateTime AND :endDateTime")
    Page<Payment> findMyPaymentsByPeriod(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
    );

}
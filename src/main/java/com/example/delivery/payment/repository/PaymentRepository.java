package com.example.delivery.payment.repository;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @EntityGraph(attributePaths = {"order"})
    Optional<Payment> findByPaymentIdAndDeletedAtIsNull(UUID paymentId);

    @Query(value = "SELECT p FROM Payment p JOIN FETCH p.order o " +
            "WHERE o.userId = :userId " +
            "AND p.deletedAt IS NULL " +
            "AND p.approvedAt BETWEEN :startDateTime AND :endDateTime",
            countQuery = "SELECT count(p) FROM Payment p JOIN p.order o " +
                    "WHERE o.userId = :userId " +
                    "AND p.deletedAt IS NULL " +
                    "AND p.approvedAt BETWEEN :startDateTime AND :endDateTime")
    Page<Payment> findMyPaymentsByPeriod(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
    );

    @Query(value = "SELECT p FROM Payment p JOIN FETCH p.order o " +
            "WHERE p.deletedAt IS NULL " + // 🌟 살아있는 결제 원장 데이터만 수립
            "AND p.approvedAt BETWEEN :startDateTime AND :endDateTime " +
            "AND (:status IS NULL OR p.status = :status)",
            countQuery = "SELECT count(p) FROM Payment p " +
                    "WHERE p.deletedAt IS NULL " +
                    "AND p.approvedAt BETWEEN :startDateTime AND :endDateTime " +
                    "AND (:status IS NULL OR p.status = :status)")
    Page<Payment> findAdminPaymentsByFilters(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("status") PaymentStatus status,
            Pageable pageable
    );

}
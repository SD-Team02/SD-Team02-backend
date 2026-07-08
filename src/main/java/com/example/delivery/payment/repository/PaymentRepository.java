package com.example.delivery.payment.repository;

import com.example.delivery.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {


}
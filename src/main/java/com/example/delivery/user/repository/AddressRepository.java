package com.example.delivery.user.repository;

import com.example.delivery.user.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    Page<Address> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Page<Address> findByDeletedAtIsNull(Pageable pageable);

    Optional<Address> findByIdAndDeletedAtIsNull(UUID addressId);
}

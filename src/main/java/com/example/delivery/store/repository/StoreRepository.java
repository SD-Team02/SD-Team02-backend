package com.example.delivery.store.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, UUID> {
}

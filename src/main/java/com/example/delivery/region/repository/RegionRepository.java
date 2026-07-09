package com.example.delivery.region.repository;

import com.example.delivery.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    boolean existsByName(String name);
}

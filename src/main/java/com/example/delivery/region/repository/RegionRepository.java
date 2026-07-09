package com.example.delivery.region.repository;

import com.example.delivery.region.entity.Region;
import com.example.delivery.region.entity.RegionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    boolean existsByName(String name);
    boolean existsByNameAndDeletedAtIsNull(String name);
    Page<Region> findAllByStatusAndDeletedAtIsNull(RegionStatus status, Pageable pageable);
    Optional<Region> findByRegionIdAndDeletedAtIsNull(UUID regionId);
}

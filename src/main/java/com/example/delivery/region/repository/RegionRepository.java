package com.example.delivery.region.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.region.entity.Region;

public interface RegionRepository extends JpaRepository<Region, UUID>, RegionRepositoryCustom {
    boolean existsByNameAndParentRegionIdAndDeletedAtIsNull(String name, UUID parentRegionId);
    boolean existsByNameAndParentRegionIdIsNullAndDeletedAtIsNull(String name);
    Optional<Region> findByRegionIdAndDeletedAtIsNull(UUID regionId);
    boolean existsByNameAndParentRegionIdAndRegionIdNotAndDeletedAtIsNull(String name, UUID parentRegionId, UUID regionId);
    boolean existsByNameAndParentRegionIdIsNullAndRegionIdNotAndDeletedAtIsNull(String name, UUID regionId);
    boolean existsByParentRegionIdAndDeletedAtIsNull(UUID parentRegionId);
}

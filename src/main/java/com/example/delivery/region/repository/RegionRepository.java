package com.example.delivery.region.repository;

import com.example.delivery.region.entity.Region;
import com.example.delivery.region.entity.RegionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    boolean existsByNameAndParentRegionIdAndDeletedAtIsNull(String name, UUID parentRegionId);
    boolean existsByNameAndParentRegionIdIsNullAndDeletedAtIsNull(String name);
    Page<Region> findAllByStatusAndDeletedAtIsNull(RegionStatus status, Pageable pageable);
    Optional<Region> findByRegionIdAndDeletedAtIsNull(UUID regionId);
    List<Region> findByRegionIdInAndDeletedAtIsNull(List<UUID> regionIds);
    boolean existsByNameAndParentRegionIdAndRegionIdNotAndDeletedAtIsNull(String name, UUID parentRegionId, UUID regionId);
    boolean existsByNameAndParentRegionIdIsNullAndRegionIdNotAndDeletedAtIsNull(String name, UUID regionId);
    boolean existsByParentRegionIdAndDeletedAtIsNull(UUID parentRegionId);
}

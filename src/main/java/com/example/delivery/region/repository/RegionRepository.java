package com.example.delivery.region.repository;

import com.example.delivery.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID>, RegionRepositoryCustom {
    boolean existsByNameAndParentRegionIdAndDeletedAtIsNull(String name, UUID parentRegionId);
    boolean existsByNameAndParentRegionIdIsNullAndDeletedAtIsNull(String name);
    Optional<Region> findByRegionIdAndDeletedAtIsNull(UUID regionId);
    List<Region> findByRegionIdInAndDeletedAtIsNull(List<UUID> regionIds);
    boolean existsByNameAndParentRegionIdAndRegionIdNotAndDeletedAtIsNull(String name, UUID parentRegionId, UUID regionId);
    boolean existsByNameAndParentRegionIdIsNullAndRegionIdNotAndDeletedAtIsNull(String name, UUID regionId);
    boolean existsByParentRegionIdAndDeletedAtIsNull(UUID parentRegionId);
}

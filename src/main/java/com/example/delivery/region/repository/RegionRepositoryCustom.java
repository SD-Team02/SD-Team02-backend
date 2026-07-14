package com.example.delivery.region.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.delivery.region.dto.response.ResGetRegionDto;
import com.example.delivery.region.entity.RegionStatus;

public interface RegionRepositoryCustom {

    /**
     * 전체 지역을 상위 지역명과 함께 조회한다.
     * parent_region_id는 매핑된 연관관계가 아닌 raw ID이므로, Region을 self-join(ON)하여
     * 상위 지역명을 단일 쿼리로 가져온다. (기존 건별 조회로 인한 N+1 제거)
     */
    Page<ResGetRegionDto> findAllWithParentName(RegionStatus status, Pageable pageable);
}

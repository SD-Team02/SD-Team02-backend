package com.example.delivery.region.service;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.region.dto.request.ReqCreateRegionDto;
import com.example.delivery.region.dto.response.ResCreateRegionDto;
import com.example.delivery.region.dto.response.ResGetRegionDto;
import com.example.delivery.region.entity.Region;
import com.example.delivery.region.entity.RegionStatus;
import com.example.delivery.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;

    //지역 등록
    @Transactional
    public ResCreateRegionDto createRegion(ReqCreateRegionDto reqCreateRegionDto) {
        checkDuplicate(reqCreateRegionDto.getName());

        //상위 지역 존재 여부 확인
        String parentName = null;
        if (reqCreateRegionDto.getParentRegionId() != null) {
            Region parent = regionRepository.findByRegionIdAndDeletedAtIsNull(reqCreateRegionDto.getParentRegionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.REGION_PARENT_NOT_FOUND));

            parentName = parent.getName();
        }

        try{
            Region region = new Region(reqCreateRegionDto.getName(),reqCreateRegionDto.getParentRegionId());

            return ResCreateRegionDto.from(regionRepository.save(region), parentName);
        } catch (DataIntegrityViolationException e){
//            TODO : DB 레벨에서 지역명 중복 방어 의도로 DataIntegrityViolationException을 잡고 있지만
//                NOUT NULL 등 다양한 원인으로 발생할 수 있을 것 같아 다른 에러 메시지를 고려할 필요 있어보임
//            (category 생성 부분도 마찬가지)
            throw new BusinessException(ErrorCode.REGION_ALREADY_EXISTS);
        }
    }

    //전체 지역 조회
    @Transactional(readOnly = true)
    public Page<ResGetRegionDto> getAllRegions(RegionStatus status, Pageable pageable) {
        return regionRepository.findAllByStatusAndDeletedAtIsNull(status, pageable)
                .map(region -> {
                    String parentName = null;
                    if (region.getParentRegionId() != null) {
                        parentName = regionRepository.findByRegionIdAndDeletedAtIsNull(region.getParentRegionId())
                                .map(Region::getName)
                                .orElse(null);
                    }
                    return ResGetRegionDto.from(region, parentName);
                });
    }


    /*
    [공통 메서드]
     */
    //지역명 중복 여부 확인 method
    private void checkDuplicate(String name) {
        if(regionRepository.existsByNameAndDeletedAtIsNull(name)){
            throw new BusinessException(ErrorCode.REGION_ALREADY_EXISTS);
        }
    }
}

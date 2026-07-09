package com.example.delivery.region.service;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.region.dto.request.ReqCreateRegionDto;
import com.example.delivery.region.dto.response.ResCreateRegionDto;
import com.example.delivery.region.entity.Region;
import com.example.delivery.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
            Region parent = regionRepository.findById(reqCreateRegionDto.getParentRegionId())
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


    /*
    [공통 메서드]
     */
    //지역명 중복 여부 확인 method
    private void checkDuplicate(String name) {
        if(regionRepository.existsByName(name)){
            throw new BusinessException(ErrorCode.REGION_ALREADY_EXISTS);
        }
    }
}

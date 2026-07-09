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

        try{
            Region region = new Region(reqCreateRegionDto.getName(),reqCreateRegionDto.getParentRegionId());

            return ResCreateRegionDto.from(regionRepository.save(region));
        } catch (DataIntegrityViolationException e){
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

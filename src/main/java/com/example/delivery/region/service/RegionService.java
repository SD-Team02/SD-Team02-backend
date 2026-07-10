package com.example.delivery.region.service;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.region.dto.request.ReqCreateRegionDto;
import com.example.delivery.region.dto.request.ReqUpdateRegionDto;
import com.example.delivery.region.dto.response.ResCreateRegionDto;
import com.example.delivery.region.dto.response.ResDeleteRegionDto;
import com.example.delivery.region.dto.response.ResGetRegionDto;
import com.example.delivery.region.dto.response.ResUpdateRegionDto;
import com.example.delivery.region.entity.Region;
import com.example.delivery.region.entity.RegionStatus;
import com.example.delivery.region.repository.RegionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;

    //지역 등록
    @Transactional
    public ResCreateRegionDto createRegion(ReqCreateRegionDto reqCreateRegionDto) {
        checkDuplicateForCreate(reqCreateRegionDto.getName(), reqCreateRegionDto.getParentRegionId());

        //상위 지역명 구하기
        String parentName = null;

        //존재하지 않는 상위 지역으로 등록 요청 시 실패
        if (reqCreateRegionDto.getParentRegionId() != null){
            Region parent = getParentRegion(reqCreateRegionDto.getParentRegionId());
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
                    String parentName = getParentRegionName(region.getParentRegionId());
                    return ResGetRegionDto.from(region, parentName);
                });
    }

    //지역 상세 조회
    @Transactional(readOnly = true)
    public ResGetRegionDto getRegion(UUID regionId) {
        Region region = regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));

        String parentName = getParentRegionName(region.getParentRegionId());
        return ResGetRegionDto.from(region, parentName);
    }

    //지역 수정
    @Transactional
    public ResUpdateRegionDto updateRegion(UUID regionId, @Valid ReqUpdateRegionDto reqUpdateRegionDto) {
        Region region = regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));

        if(!Objects.equals(region.getName(), reqUpdateRegionDto.getName())
                || !Objects.equals(region.getParentRegionId(), reqUpdateRegionDto.getParentRegionId())){
            //기존 데이터의 지역명으로의 변경 막기
            checkDuplicateForUpdate(reqUpdateRegionDto.getName(), reqUpdateRegionDto.getParentRegionId(), regionId);
        }

        String parentName = null;
        if (reqUpdateRegionDto.getParentRegionId() != null) {
            parentName = getParentRegion(reqUpdateRegionDto.getParentRegionId()).getName();
        }

        region.update(reqUpdateRegionDto.getName(),reqUpdateRegionDto.getParentRegionId(),reqUpdateRegionDto.getStatus());
        regionRepository.saveAndFlush(region);

        return ResUpdateRegionDto.from(region,parentName);
    }

    //지역 삭제
    @Transactional
    public ResDeleteRegionDto deleteRegion(UUID regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));

        if(region.isDeleted()){
            throw new BusinessException(ErrorCode.REGION_ALREADY_DELETED);
        }

        region.softDelete(0L); // TODO: 실제 로그인한 유저 ID를 넣어주어야 함
        return ResDeleteRegionDto.from(region);
    }


    /*
    [공통 메서드]
     */
    //지역 등록 시 중복 여부 확인
    // 같은 상위 지역 아래 같은 지역명은 불가
    private void checkDuplicateForCreate(String name, UUID parentId) {
        boolean exists = (parentId == null) 
            ? regionRepository.existsByNameAndParentRegionIdIsNullAndDeletedAtIsNull(name) 
            : regionRepository.existsByNameAndParentRegionIdAndDeletedAtIsNull(name, parentId);
        
        if (exists) {
            throw new BusinessException(ErrorCode.REGION_ALREADY_EXISTS);
        }
    }

    //지역 수정 시 중복 여부 확인
    private void checkDuplicateForUpdate(String name, UUID parentId, UUID regionId) {
        boolean exists = (parentId == null)
                ? regionRepository.existsByNameAndParentRegionIdIsNullAndRegionIdNotAndDeletedAtIsNull(name, regionId)
                : regionRepository.existsByNameAndParentRegionIdAndRegionIdNotAndDeletedAtIsNull(name, parentId, regionId);

        if (exists) {
            throw new BusinessException(ErrorCode.REGION_ALREADY_EXISTS);
        }
    }

    //상위 지역 조회 method
    private Region getParentRegion(UUID parentRegionId){
        return regionRepository.findByRegionIdAndDeletedAtIsNull(parentRegionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_PARENT_NOT_FOUND));
    }

    //상위 지역명 얻는 method
    private String getParentRegionName(UUID parentRegionId){
        String parentName = null;

        if (parentRegionId != null){
            parentName = regionRepository.findByRegionIdAndDeletedAtIsNull(parentRegionId)
                    .map(Region::getName)
                    .orElse(null);
        }

        return parentName;
    }
}
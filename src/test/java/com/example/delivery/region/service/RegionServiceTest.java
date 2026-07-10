package com.example.delivery.region.service;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.region.dto.request.ReqCreateRegionDto;
import com.example.delivery.region.dto.request.ReqUpdateRegionDto;
import com.example.delivery.region.dto.response.ResUpdateRegionDto;
import com.example.delivery.region.entity.RegionStatus;
import com.example.delivery.region.repository.RegionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.delivery.region.dto.response.ResCreateRegionDto;
import com.example.delivery.region.entity.Region;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionService regionService;

    @Test
    @DisplayName("지역 생성 - 최상위 지역의 경우 지역 생성에 성공한다.")
    void createRegion_Success_NoParent() {
        // given
        ReqCreateRegionDto dto = new ReqCreateRegionDto("대한민국", null);
        when(regionRepository.existsByNameAndParentRegionIdIsNull("대한민국")).thenReturn(false);
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ResCreateRegionDto result = regionService.createRegion(dto);

        // then
        assertNotNull(result);
        assertEquals("대한민국", result.getName());
        verify(regionRepository).existsByNameAndParentRegionIdIsNull("대한민국");
        verify(regionRepository).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 생성 - 상위 지역이 존재하는 경우 지역 생성에 성공한다.")
    void createRegion_Success_WithParent() {
        // given
        UUID parentId = UUID.randomUUID();
        ReqCreateRegionDto dto = new ReqCreateRegionDto("강남구", parentId);
        when(regionRepository.existsByNameAndParentRegionId("강남구", parentId)).thenReturn(false);
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.of(new Region("서울시")));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ResCreateRegionDto result = regionService.createRegion(dto);

        // then
        assertNotNull(result);
        assertEquals("강남구", result.getName());
        assertEquals(parentId, result.getParentRegionId());
        assertEquals("서울시", result.getParentRegionName());
        verify(regionRepository).existsByNameAndParentRegionId("강남구", parentId);
        verify(regionRepository).findByRegionIdAndDeletedAtIsNull(parentId);
        verify(regionRepository).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 생성 - 지역명이 중복되면 예외를 발생시킨다.")
    void createRegion_DuplicateName() {
        // given
        ReqCreateRegionDto dto = new ReqCreateRegionDto("서울", null);
        when(regionRepository.existsByNameAndParentRegionIdIsNull("서울")).thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> regionService.createRegion(dto));
        verify(regionRepository, never()).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 생성 - 존재하지 않는 상위 지역으로 지역 생성 시 예외를 발생시킨다.")
    void createRegion_ParentNotFound() {
        // given
        UUID parentId = UUID.randomUUID();
        ReqCreateRegionDto dto = new ReqCreateRegionDto("서브지역", parentId);

        when(regionRepository.existsByNameAndParentRegionId("서브지역", parentId)).thenReturn(false);
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> regionService.createRegion(dto));
        verify(regionRepository, never()).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 수정 - 성공한다.")
    void updateRegion_Success() {
        // given
        UUID regionId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구 수정", null, RegionStatus.ACTIVE);
        Region existingRegion = new Region("강남구", null);
        
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(existingRegion));
        when(regionRepository.existsByNameAndRegionIdNot("강남구 수정", regionId)).thenReturn(false);

        // when
        ResUpdateRegionDto result = regionService.updateRegion(regionId, dto);

        // then
        assertNotNull(result);
        assertEquals("강남구 수정", result.getName());
        verify(regionRepository).findById(regionId);
        verify(regionRepository).existsByNameAndRegionIdNot("강남구 수정", regionId);
        verify(regionRepository).saveAndFlush(any(Region.class));
    }

    @Test
    @DisplayName("지역 수정 - 존재하지 않는 지역인 경우 예외를 발생시킨다.")
    void updateRegion_RegionNotFound() {
        // given
        UUID regionId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구 수정", null, RegionStatus.ACTIVE);
        when(regionRepository.findById(regionId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> regionService.updateRegion(regionId, dto));
    }

    @Test
    @DisplayName("지역 수정 - 지역명이 중복되면 예외를 발생시킨다.")
    void updateRegion_DuplicateName() {
        // given
        UUID regionId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구 수정", null, RegionStatus.ACTIVE);
        Region existingRegion = new Region("강남구", null);
        
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(existingRegion));
        when(regionRepository.existsByNameAndRegionIdNot("강남구 수정", regionId)).thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> regionService.updateRegion(regionId, dto));
        verify(regionRepository, never()).saveAndFlush(any(Region.class));
    }
}

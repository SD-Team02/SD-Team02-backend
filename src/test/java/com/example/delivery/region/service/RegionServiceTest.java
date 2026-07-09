package com.example.delivery.region.service;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.region.dto.request.ReqCreateRegionDto;
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
        when(regionRepository.existsByName("대한민국")).thenReturn(false);
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ResCreateRegionDto result = regionService.createRegion(dto);

        // then
        assertNotNull(result);
        assertEquals("대한민국", result.getName());
        verify(regionRepository).existsByName("대한민국");
        verify(regionRepository).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 생성 - 상위 지역이 존재하는 경우 지역 생성에 성공한다.")
    void createRegion_Success_WithParent() {
        // given
        UUID parentId = UUID.randomUUID();
        ReqCreateRegionDto dto = new ReqCreateRegionDto("강남구", parentId);
        when(regionRepository.existsByName("강남구")).thenReturn(false);
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.of(new Region("서울시")));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ResCreateRegionDto result = regionService.createRegion(dto);

        // then
        assertNotNull(result);
        assertEquals("강남구", result.getName());
        assertEquals(parentId, result.getParentRegionId());
        assertEquals("서울시", result.getParentRegionName());
        verify(regionRepository).existsByName("강남구");
        verify(regionRepository).findByRegionIdAndDeletedAtIsNull(parentId);
        verify(regionRepository).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 생성 - 지역명이 중복되면 예외를 발생시킨다.")
    void createRegion_DuplicateName() {
        // given
        ReqCreateRegionDto dto = new ReqCreateRegionDto("서울", null);
        when(regionRepository.existsByName("서울")).thenReturn(true);

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
        
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> regionService.createRegion(dto));
    }
}

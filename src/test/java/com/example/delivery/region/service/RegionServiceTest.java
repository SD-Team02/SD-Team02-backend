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
        when(regionRepository.existsByNameAndParentRegionIdIsNullAndDeletedAtIsNull("대한민국")).thenReturn(false);
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ResCreateRegionDto result = regionService.createRegion(dto);

        // then
        assertNotNull(result);
        assertEquals("대한민국", result.getName());
        verify(regionRepository).existsByNameAndParentRegionIdIsNullAndDeletedAtIsNull("대한민국");
        verify(regionRepository).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 생성 - 상위 지역이 존재하는 경우 지역 생성에 성공한다.")
    void createRegion_Success_WithParent() {
        // given
        UUID parentId = UUID.randomUUID();
        ReqCreateRegionDto dto = new ReqCreateRegionDto("강남구", parentId);
        when(regionRepository.existsByNameAndParentRegionIdAndDeletedAtIsNull("강남구", parentId)).thenReturn(false);
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.of(new Region("서울시")));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ResCreateRegionDto result = regionService.createRegion(dto);

        // then
        assertNotNull(result);
        assertEquals("강남구", result.getName());
        assertEquals(parentId, result.getParentRegionId());
        assertEquals("서울시", result.getParentRegionName());
        verify(regionRepository).existsByNameAndParentRegionIdAndDeletedAtIsNull("강남구", parentId);
        verify(regionRepository).findByRegionIdAndDeletedAtIsNull(parentId);
        verify(regionRepository).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 생성 - 지역명이 중복되면 예외를 발생시킨다.")
    void createRegion_DuplicateName() {
        // given
        ReqCreateRegionDto dto = new ReqCreateRegionDto("서울", null);
        when(regionRepository.existsByNameAndParentRegionIdIsNullAndDeletedAtIsNull("서울")).thenReturn(true);

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

        when(regionRepository.existsByNameAndParentRegionIdAndDeletedAtIsNull("서브지역", parentId)).thenReturn(false);
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> regionService.createRegion(dto));
        verify(regionRepository, never()).save(any(Region.class));
    }

    @Test
    @DisplayName("지역 상세 조회 - 성공한다.")
    void getRegion_Success() {
        // given
        UUID regionId = UUID.randomUUID();
        Region region = new Region("강남구", null);

        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.of(region));

        // when
        var result = regionService.getRegion(regionId);

        // then
        assertNotNull(result);
        assertEquals("강남구", result.getName());
        verify(regionRepository).findByRegionIdAndDeletedAtIsNull(regionId);
    }

    @Test
    @DisplayName("지역 상세 조회 - 존재하지 않는 지역인 경우 예외를 발생시킨다.")
    void getRegion_RegionNotFound() {
        // given
        UUID regionId = UUID.randomUUID();
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> regionService.getRegion(regionId));
        verify(regionRepository).findByRegionIdAndDeletedAtIsNull(regionId);
    }

    @Test
    @DisplayName("지역 수정 - 성공한다.")
    void updateRegion_Success() {
        // given
        UUID regionId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구 수정", null, RegionStatus.ACTIVE);
        Region existingRegion = new Region("강남구", null);
        
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.of(existingRegion));
        when(regionRepository.existsByNameAndParentRegionIdIsNullAndRegionIdNotAndDeletedAtIsNull("강남구 수정", regionId)).thenReturn(false);

        // when
        ResUpdateRegionDto result = regionService.updateRegion(regionId, dto);

        // then
        assertNotNull(result);
        assertEquals("강남구 수정", result.getName());
        verify(regionRepository).findByRegionIdAndDeletedAtIsNull(regionId);
        verify(regionRepository).existsByNameAndParentRegionIdIsNullAndRegionIdNotAndDeletedAtIsNull("강남구 수정", regionId);
        verify(regionRepository).saveAndFlush(any(Region.class));
    }

    @Test
    @DisplayName("지역 수정 - 존재하지 않는 지역인 경우 예외를 발생시킨다.")
    void updateRegion_RegionNotFound() {
        // given
        UUID regionId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구 수정", null, RegionStatus.ACTIVE);
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> regionService.updateRegion(regionId, dto));
    }

    @Test
    @DisplayName("지역 수정 - 존재하지 않는 상위 지역으로 수정 시 예외를 발생시킨다.")
    void updateRegion_ParentNotFound() {
        // given
        UUID regionId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구 수정", parentId, RegionStatus.ACTIVE);
        Region existingRegion = new Region("강남구", null);
        
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.of(existingRegion));
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(parentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> regionService.updateRegion(regionId, dto));
        verify(regionRepository, never()).saveAndFlush(any(Region.class));
    }

    @Test
    @DisplayName("지역 수정 - 지역명이 중복되면 예외를 발생시킨다.")
    void updateRegion_DuplicateName() {
        // given
        UUID regionId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구 수정", null, RegionStatus.ACTIVE);
        Region existingRegion = new Region("강남구", null);
        
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.of(existingRegion));
        when(regionRepository.existsByNameAndParentRegionIdIsNullAndRegionIdNotAndDeletedAtIsNull("강남구 수정", regionId)).thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> regionService.updateRegion(regionId, dto));
        verify(regionRepository, never()).saveAndFlush(any(Region.class));
    }

    @Test
    @DisplayName("지역 수정 - 다른 상위 지역 아래에 동일한 이름이 이미 존재하면 수정에 성공한다.")
    void updateRegion_Success_SameNameDifferentParent() {
        // given
        UUID regionId = UUID.randomUUID();
        UUID otherParentId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구", otherParentId, RegionStatus.ACTIVE);
        Region existingRegion = new Region("기존이름", null);
        
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.of(existingRegion));
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(otherParentId)).thenReturn(Optional.of(new Region("부모지역")));

        when(regionRepository.existsByNameAndParentRegionIdAndRegionIdNotAndDeletedAtIsNull("강남구", otherParentId, regionId)).thenReturn(false);

        // when
        ResUpdateRegionDto result = regionService.updateRegion(regionId, dto);

        // then
        assertNotNull(result);
        assertEquals("강남구", result.getName());
        verify(regionRepository).findByRegionIdAndDeletedAtIsNull(regionId);
        verify(regionRepository).saveAndFlush(any(Region.class));
    }

    @Test
    @DisplayName("지역 수정 - 순환 참조가 발생하는 경우 예외를 발생시킨다.")
    void updateRegion_CircularReference() {
        // given
        UUID regionId = UUID.randomUUID();
        UUID childRegionId = UUID.randomUUID();
        ReqUpdateRegionDto dto = new ReqUpdateRegionDto("강남구 수정", childRegionId, RegionStatus.ACTIVE);
        Region existingRegion = new Region("강남구", null);
        Region childRegion = new Region("역삼동", regionId);

        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.of(existingRegion));
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(childRegionId)).thenReturn(Optional.of(childRegion));
        when(regionRepository.findByRegionIdAndDeletedAtIsNull(regionId)).thenReturn(Optional.of(existingRegion));

        // when & then
        assertThrows(BusinessException.class, () -> regionService.updateRegion(regionId, dto));
    }

    @Test
    @DisplayName("지역 삭제 - 성공한다.")
    void deleteRegion_Success() {
        // given
        UUID regionId = UUID.randomUUID();
        Region existingRegion = new Region("강남구", null);
        
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(existingRegion));

        // when
        var result = regionService.deleteRegion(regionId);

        // then
        assertNotNull(result);
        assertTrue(existingRegion.isDeleted());
        verify(regionRepository).findById(regionId);
    }

    @Test
    @DisplayName("지역 삭제 - 존재하지 않는 지역인 경우 예외를 발생시킨다.")
    void deleteRegion_RegionNotFound() {
        // given
        UUID regionId = UUID.randomUUID();
        when(regionRepository.findById(regionId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> regionService.deleteRegion(regionId));
    }

    @Test
    @DisplayName("지역 삭제 - 이미 삭제된 지역인 경우 예외를 발생시킨다.")
    void deleteRegion_AlreadyDeleted() {
        // given
        UUID regionId = UUID.randomUUID();
        Region existingRegion = new Region("강남구", null);
        existingRegion.softDelete(1L);
        
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(existingRegion));

        // when & then
        assertThrows(BusinessException.class, () -> regionService.deleteRegion(regionId));
    }

    @Test
    @DisplayName("지역 삭제 - 하위 지역이 존재하는 경우 예외를 발생시킨다.")
    void deleteRegion_HasChildren() {
        // given
        UUID regionId = UUID.randomUUID();
        Region existingRegion = new Region("강남구", null);
        
        when(regionRepository.findById(regionId)).thenReturn(Optional.of(existingRegion));
        when(regionRepository.existsByParentRegionIdAndDeletedAtIsNull(regionId)).thenReturn(true);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> regionService.deleteRegion(regionId));
        assertEquals(ErrorCode.REGION_HAS_CHILDREN, exception.getErrorCode());
        verify(regionRepository).findById(regionId);
        verify(regionRepository).existsByParentRegionIdAndDeletedAtIsNull(regionId);
    }
}

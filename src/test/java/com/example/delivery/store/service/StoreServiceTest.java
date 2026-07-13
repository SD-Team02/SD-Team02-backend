package com.example.delivery.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import com.example.delivery.category.entity.Category;
import com.example.delivery.category.repository.CategoryRepository;
import com.example.delivery.region.entity.Region;
import com.example.delivery.region.repository.RegionRepository;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.store.dto.request.ReqCreateStoreDto;
import com.example.delivery.store.dto.response.ResCreateStoreDto;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private StoreService storeService;

    private Long userId;
    private ReqCreateStoreDto reqDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        reqDto = new ReqCreateStoreDto(
            UUID.randomUUID(), UUID.randomUUID(), "가게이름", "주소", "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0), null
        );
    }

    @Test
    @DisplayName("가게 등록 성공 - 신규 생성")
    void createStore_Success_New() {
        // given
        when(storeRepository.findByName(reqDto.getName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(reqDto.getCategoryId())).thenReturn(Optional.of(new Category("한식")));
        when(regionRepository.findById(reqDto.getRegionId())).thenReturn(Optional.of(new Region("강남구")));
        
        // when
        ResCreateStoreDto result = storeService.createStore(userId, reqDto);

        // then
        assertNotNull(result);
        assertEquals(reqDto.getName(), result.getName());
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("가게 등록 실패 - 이미 존재하는 가게")
    void createStore_Fail_AlreadyExists() {
        // given
        Store existingStore = new Store(userId, reqDto.getCategoryId(), reqDto.getRegionId(), reqDto.getName(), reqDto.getAddress(), reqDto.getPhone(), reqDto.getOpenTime(), reqDto.getCloseTime());
        when(storeRepository.findByName(reqDto.getName())).thenReturn(Optional.of(existingStore));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> storeService.createStore(userId, reqDto));
        assertEquals(ErrorCode.STORE_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게 등록 실패 - 존재하지 않는 카테고리")
    void createStore_Fail_CategoryNotFound() {
        // given
        when(storeRepository.findByName(reqDto.getName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(reqDto.getCategoryId())).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> storeService.createStore(userId, reqDto));
        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게 등록 실패 - 존재하지 않는 지역")
    void createStore_Fail_RegionNotFound() {
        // given
        when(storeRepository.findByName(reqDto.getName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(reqDto.getCategoryId())).thenReturn(Optional.of(new Category("한식")));
        when(regionRepository.findById(reqDto.getRegionId())).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> storeService.createStore(userId, reqDto));
        assertEquals(ErrorCode.REGION_NOT_FOUND, exception.getErrorCode());
    }
}

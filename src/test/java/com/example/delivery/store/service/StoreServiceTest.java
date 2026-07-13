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
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.delivery.store.dto.request.ReqCreateStoreDto;
import com.example.delivery.store.dto.request.ReqUpdateStoreDto;
import com.example.delivery.store.dto.response.ResCreateStoreDto;
import com.example.delivery.store.dto.response.ResDeleteStoreDto;
import com.example.delivery.store.dto.response.ResGetStoreDto;
import com.example.delivery.store.dto.response.ResSearchStoreDto;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.entity.StoreStatus;
import com.example.delivery.store.repository.StoreRepository;
import com.example.delivery.user.entity.Role;
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

    @Test
    @DisplayName("전체 가게 조회 성공 - 카테고리/지역명을 배치로 조회한다")
    void getAllStores_Success_BatchResolvesNames() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        UUID categoryId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        Store store = new Store(userId, categoryId, regionId, "가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));

        Category category = mock(Category.class);
        when(category.getCategoryId()).thenReturn(categoryId);
        when(category.getName()).thenReturn("한식");
        Region region = mock(Region.class);
        when(region.getRegionId()).thenReturn(regionId);
        when(region.getName()).thenReturn("강남구");

        when(storeRepository.findByStatusAndDeletedAtIsNull(StoreStatus.OPEN, pageable))
                .thenReturn(new PageImpl<>(List.of(store)));
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));
        when(regionRepository.findAllById(anyList())).thenReturn(List.of(region));

        // when
        Page<ResGetStoreDto> result = storeService.getAllStores(StoreStatus.OPEN, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("한식", result.getContent().get(0).getCategoryName());
        assertEquals("강남구", result.getContent().get(0).getRegionName());
        // 가게마다 개별 조회(N+1)하지 않고 배치로 한 번에 조회해야 함
        verify(categoryRepository, never()).findById(any());
        verify(regionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("전체 가게 조회 성공 - 결과가 여러 건이어도 카테고리/지역 배치 조회는 1번만 호출된다 (N+1 방지)")
    void getAllStores_Success_BatchCalledOnceForMultipleStores() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();
        UUID regionId1 = UUID.randomUUID();
        UUID regionId2 = UUID.randomUUID();
        Store store1 = new Store(userId, categoryId1, regionId1, "가게1", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        Store store2 = new Store(userId, categoryId2, regionId2, "가게2", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));

        Category category1 = mock(Category.class);
        when(category1.getCategoryId()).thenReturn(categoryId1);
        when(category1.getName()).thenReturn("한식");
        Category category2 = mock(Category.class);
        when(category2.getCategoryId()).thenReturn(categoryId2);
        when(category2.getName()).thenReturn("중식");
        Region region1 = mock(Region.class);
        when(region1.getRegionId()).thenReturn(regionId1);
        when(region1.getName()).thenReturn("강남구");
        Region region2 = mock(Region.class);
        when(region2.getRegionId()).thenReturn(regionId2);
        when(region2.getName()).thenReturn("서초구");

        when(storeRepository.findByStatusAndDeletedAtIsNull(StoreStatus.OPEN, pageable))
                .thenReturn(new PageImpl<>(List.of(store1, store2)));
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category1, category2));
        when(regionRepository.findAllById(anyList())).thenReturn(List.of(region1, region2));

        // when
        Page<ResGetStoreDto> result = storeService.getAllStores(StoreStatus.OPEN, pageable);

        // then
        assertEquals(2, result.getTotalElements());
        // 가게 수와 무관하게 카테고리/지역 조회는 각각 정확히 1번만 호출되어야 함 (N+1이면 가게 수만큼 호출됨)
        verify(categoryRepository, times(1)).findAllById(argThat(ids ->
                ((List<UUID>) ids).containsAll(List.of(categoryId1, categoryId2))));
        verify(regionRepository, times(1)).findAllById(argThat(ids ->
                ((List<UUID>) ids).containsAll(List.of(regionId1, regionId2))));
    }

    @Test
    @DisplayName("가게 검색 성공 - keyword만 입력, 결과가 여러 건이어도 카테고리/지역 배치 조회는 1번만 호출된다 (N+1 방지)")
    void searchStores_Success_KeywordOnly_BatchCalledOnceForMultipleStores() {
        // given
        String keyword = "떡볶이";
        Pageable pageable = PageRequest.of(0, 10);
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();
        UUID regionId1 = UUID.randomUUID();
        UUID regionId2 = UUID.randomUUID();
        Store store1 = new Store(userId, categoryId1, regionId1, "떡볶이집1", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        Store store2 = new Store(userId, categoryId2, regionId2, "떡볶이집2", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));

        Category category1 = mock(Category.class);
        when(category1.getCategoryId()).thenReturn(categoryId1);
        when(category1.getName()).thenReturn("분식");
        Category category2 = mock(Category.class);
        when(category2.getCategoryId()).thenReturn(categoryId2);
        when(category2.getName()).thenReturn("한식");
        Region region1 = mock(Region.class);
        when(region1.getRegionId()).thenReturn(regionId1);
        when(region1.getName()).thenReturn("강남구");
        Region region2 = mock(Region.class);
        when(region2.getRegionId()).thenReturn(regionId2);
        when(region2.getName()).thenReturn("서초구");

        when(categoryRepository.findByNameContainingAndDeletedAtIsNull(keyword)).thenReturn(List.of());
        when(storeRepository.searchStores(eq(keyword), isNull(), anyList(), eq(StoreStatus.OPEN), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(store1, store2)));
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category1, category2));
        when(regionRepository.findAllById(anyList())).thenReturn(List.of(region1, region2));

        // when
        Page<ResSearchStoreDto> result = storeService.searchStores(keyword, null, StoreStatus.OPEN, pageable);

        // then
        assertEquals(2, result.getTotalElements());
        // 가게 수와 무관하게 카테고리/지역 조회는 각각 정확히 1번만 호출되어야 함 (N+1이면 가게 수만큼 호출됨)
        verify(categoryRepository, times(1)).findAllById(argThat(ids ->
                ((List<UUID>) ids).containsAll(List.of(categoryId1, categoryId2))));
        verify(regionRepository, times(1)).findAllById(argThat(ids ->
                ((List<UUID>) ids).containsAll(List.of(regionId1, regionId2))));
        verify(categoryRepository, never()).findById(any());
        verify(regionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("가게 검색 실패 - keyword, categoryId 둘 다 없음")
    void searchStores_Fail_NoCondition() {
        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> storeService.searchStores(null, null, StoreStatus.OPEN, Pageable.unpaged()));
        assertEquals(ErrorCode.STORE_SEARCH_CONDITION_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게 검색 성공 - keyword만 입력")
    void searchStores_Success_KeywordOnly() {
        // given
        String keyword = "떡볶이";
        Pageable pageable = PageRequest.of(0, 10);
        UUID categoryId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        Store store = new Store(userId, categoryId, regionId, "떡볶이집", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));

        Category category = mock(Category.class);
        when(category.getCategoryId()).thenReturn(categoryId);
        when(category.getName()).thenReturn("분식");
        Region region = mock(Region.class);
        when(region.getRegionId()).thenReturn(regionId);
        when(region.getName()).thenReturn("강남구");

        when(categoryRepository.findByNameContainingAndDeletedAtIsNull(keyword)).thenReturn(List.of(category));
        when(storeRepository.searchStores(eq(keyword), isNull(), anyList(), eq(StoreStatus.OPEN), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(store)));
        // 페이지 안의 가게들의 카테고리/지역명은 store별 findById가 아니라 findAllById로 한 번에 조회됨
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));
        when(regionRepository.findAllById(anyList())).thenReturn(List.of(region));

        // when
        Page<ResSearchStoreDto> result = storeService.searchStores(keyword, null, StoreStatus.OPEN, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("떡볶이집", result.getContent().get(0).getName());
        assertEquals("분식", result.getContent().get(0).getCategoryName());
        assertEquals("강남구", result.getContent().get(0).getRegionName());
        verify(categoryRepository, never()).findByCategoryIdAndDeletedAtIsNull(any());
        verify(categoryRepository, never()).findById(any());
        verify(regionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("가게 검색 성공 - categoryId만 입력, 유효한 카테고리")
    void searchStores_Success_CategoryOnly() {
        // given
        UUID categoryId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category("한식");
        Store store = new Store(userId, categoryId, regionId, "한식집", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));

        Region region = mock(Region.class);
        when(region.getRegionId()).thenReturn(regionId);
        when(region.getName()).thenReturn("강남구");

        when(categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)).thenReturn(Optional.of(category));
        when(storeRepository.searchStores(isNull(), eq(categoryId), eq(List.of()), eq(StoreStatus.OPEN), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(store)));
        when(regionRepository.findAllById(anyList())).thenReturn(List.of(region));

        // when
        Page<ResSearchStoreDto> result = storeService.searchStores(null, categoryId, StoreStatus.OPEN, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("한식집", result.getContent().get(0).getName());
        assertEquals("한식", result.getContent().get(0).getCategoryName());
        // categoryId 필터링 시 이미 조회한 카테고리명을 재사용하므로 store별 findById/findAllById는 호출되지 않아야 함
        verify(categoryRepository, never()).findById(any());
        verify(categoryRepository, never()).findAllById(any());
        verify(regionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("가게 검색 결과 없음 - categoryId에 해당하는 카테고리가 없거나 삭제됨")
    void searchStores_Empty_CategoryNotFoundOrDeleted() {
        // given
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)).thenReturn(Optional.empty());

        // when
        Page<ResSearchStoreDto> result = storeService.searchStores(null, categoryId, StoreStatus.OPEN, pageable);

        // then
        assertTrue(result.isEmpty());
        verify(storeRepository, never()).searchStores(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("가게 검색 성공 - keyword와 categoryId 모두 입력")
    void searchStores_Success_KeywordAndCategory() {
        // given
        String keyword = "떡볶이";
        UUID categoryId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category("분식");
        Store store = new Store(userId, categoryId, regionId, "엽기떡볶이", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));

        Region region = mock(Region.class);
        when(region.getRegionId()).thenReturn(regionId);
        when(region.getName()).thenReturn("강남구");

        when(categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)).thenReturn(Optional.of(category));
        when(storeRepository.searchStores(eq(keyword), eq(categoryId), eq(List.of()), eq(StoreStatus.OPEN), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(store)));
        when(regionRepository.findAllById(anyList())).thenReturn(List.of(region));

        // when
        Page<ResSearchStoreDto> result = storeService.searchStores(keyword, categoryId, StoreStatus.OPEN, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("엽기떡볶이", result.getContent().get(0).getName());
        verify(categoryRepository, never()).findById(any());
        verify(categoryRepository, never()).findAllById(any());
        verify(regionRepository, never()).findById(any());
        // categoryId가 이미 지정된 경우엔 카테고리명으로 keyword를 재검색하지 않아야 함
        verify(categoryRepository, never()).findByNameContainingAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("가게 검색 결과 없음 - 검색 조건에 맞는 가게가 없음")
    void searchStores_Empty_NoMatchingStore() {
        // given
        String keyword = "존재하지않는가게";
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findByNameContainingAndDeletedAtIsNull(keyword)).thenReturn(List.of());
        when(storeRepository.searchStores(eq(keyword), isNull(), eq(List.of()), eq(StoreStatus.OPEN), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // when
        Page<ResSearchStoreDto> result = storeService.searchStores(keyword, null, StoreStatus.OPEN, pageable);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("가게 수정 실패 - 본인 소유의 가게가 아님")
    void updateStore_Fail_NotOwner() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = new Store(1L, UUID.randomUUID(), UUID.randomUUID(), "가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        ReqUpdateStoreDto updateDto = new ReqUpdateStoreDto(
                store.getCategoryId(), store.getRegionId(), "수정된 가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0), StoreStatus.OPEN
        );

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> storeService.updateStore(updateDto, storeId, 2L, Role.OWNER));
        assertEquals(ErrorCode.STORE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게 수정 성공 - 본인 소유의 가게")
    void updateStore_Success_Owner() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = new Store(1L, UUID.randomUUID(), UUID.randomUUID(), "가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));
        when(categoryRepository.findById(store.getCategoryId())).thenReturn(Optional.of(new Category("한식")));
        when(regionRepository.findById(store.getRegionId())).thenReturn(Optional.of(new Region("강남구")));

        ReqUpdateStoreDto updateDto = new ReqUpdateStoreDto(
                store.getCategoryId(), store.getRegionId(), "수정된 가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0), StoreStatus.OPEN
        );

        // when
        ResGetStoreDto result = storeService.updateStore(updateDto, storeId, 1L, Role.OWNER);

        // then
        assertEquals("수정된 가게", result.getName());
    }

    @Test
    @DisplayName("가게 수정 성공 - MANAGER는 본인 소유가 아니어도 수정 가능")
    void updateStore_Success_ManagerBypassesOwnership() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = new Store(1L, UUID.randomUUID(), UUID.randomUUID(), "가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));
        when(categoryRepository.findById(store.getCategoryId())).thenReturn(Optional.of(new Category("한식")));
        when(regionRepository.findById(store.getRegionId())).thenReturn(Optional.of(new Region("강남구")));

        ReqUpdateStoreDto updateDto = new ReqUpdateStoreDto(
                store.getCategoryId(), store.getRegionId(), "수정된 가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0), StoreStatus.OPEN
        );

        // when
        ResGetStoreDto result = storeService.updateStore(updateDto, storeId, 999L, Role.MANAGER);

        // then
        assertEquals("수정된 가게", result.getName());
    }

    @Test
    @DisplayName("가게 삭제 실패 - 본인 소유의 가게가 아님")
    void deleteStore_Fail_NotOwner() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = new Store(1L, UUID.randomUUID(), UUID.randomUUID(), "가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> storeService.deleteStore(storeId, 2L, Role.OWNER));
        assertEquals(ErrorCode.STORE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게 삭제 성공 - 본인 소유의 가게")
    void deleteStore_Success_Owner() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = new Store(1L, UUID.randomUUID(), UUID.randomUUID(), "가게", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when
        ResDeleteStoreDto result = storeService.deleteStore(storeId, 1L, Role.OWNER);

        // then
        assertEquals("가게", result.getName());
    }
}

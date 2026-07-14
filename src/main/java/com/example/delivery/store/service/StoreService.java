package com.example.delivery.store.service;

import com.example.delivery.category.entity.Category;
import com.example.delivery.category.repository.CategoryRepository;
import com.example.delivery.region.entity.Region;
import com.example.delivery.region.repository.RegionRepository;
import com.example.delivery.store.dto.request.ReqCreateStoreDto;
import com.example.delivery.store.dto.request.ReqUpdateStoreDto;
import com.example.delivery.store.dto.response.ResCreateStoreDto;
import com.example.delivery.store.dto.response.ResDeleteStoreDto;
import com.example.delivery.store.dto.response.ResGetStoreDto;
import com.example.delivery.store.dto.response.ResSearchStoreDto;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.entity.StoreStatus;
import com.example.delivery.store.repository.StoreRepository;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.user.entity.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;

    //가게 등록
    @Transactional
    public ResCreateStoreDto createStore(Long userId, ReqCreateStoreDto reqCreateStoreDto) {

        Store store = storeRepository.findByName(reqCreateStoreDto.getName())
                .map(existingStore -> {
                    if (!existingStore.isDeleted()) {
                        throw new BusinessException(ErrorCode.STORE_ALREADY_EXISTS);
                    }
                    existingStore.restore(); // CASE3: deletedAt != null 인 경우 복구

                    // 기존 엔티티라면 정보 갱신
                    existingStore.changeInfo(
                            reqCreateStoreDto.getName(),
                            reqCreateStoreDto.getAddress(),
                            reqCreateStoreDto.getPhone(),
                            reqCreateStoreDto.getOpenTime(),
                            reqCreateStoreDto.getCloseTime()
                    );
                    existingStore.changeCategory(reqCreateStoreDto.getCategoryId());
                    existingStore.changeUser(userId);
                    existingStore.changeRegion(reqCreateStoreDto.getRegionId());
                    existingStore.changeStatus(reqCreateStoreDto.getStatus());
                    return existingStore;
                })
                .orElseGet(() -> new Store(
                        userId,
                        reqCreateStoreDto.getCategoryId(),
                        reqCreateStoreDto.getRegionId(),
                        reqCreateStoreDto.getName(),
                        reqCreateStoreDto.getAddress(),
                        reqCreateStoreDto.getPhone(),
                        reqCreateStoreDto.getOpenTime(),
                        reqCreateStoreDto.getCloseTime()
                ));

        storeRepository.save(store);

        // 3. 응답 DTO 반환
        String categoryName = resolveCategoryName(store.getCategoryId());
        String regionName = resolveRegionName(store.getRegionId());

        return ResCreateStoreDto.from(store, categoryName, regionName);
    }

    //전체 가게 조회
    @Transactional(readOnly = true)
    public Page<ResGetStoreDto> getAllStores(StoreStatus status, Pageable pageable) {
        Page<Store> storePage = storeRepository.findByStatusAndDeletedAtIsNull(status, pageable);

        // 페이지에 담긴 가게들의 카테고리/지역명을 한 번씩만 조회해 N+1을 방지한다
        Map<UUID, String> categoryNames = resolveCategoryNames(
                storePage.getContent().stream().map(Store::getCategoryId).distinct().toList());
        Map<UUID, String> regionNames = resolveRegionNames(
                storePage.getContent().stream().map(Store::getRegionId).distinct().toList());

        return storePage.map(store -> ResGetStoreDto.from(
                store,
                requireCategoryName(categoryNames, store.getCategoryId()),
                requireRegionName(regionNames, store.getRegionId())
        ));
    }

    //가게 상세 조회
    @Transactional(readOnly = true)
    public ResGetStoreDto getStore(UUID storeId) {
        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        String categoryName = resolveCategoryName(store.getCategoryId());
        String regionName = resolveRegionName(store.getRegionId());

        return ResGetStoreDto.from(store, categoryName, regionName);
    }

    //가게 수정
    @Transactional
    public ResGetStoreDto updateStore(ReqUpdateStoreDto reqUpdateStoreDto, UUID storeId, Long userId, Role role) {
        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        validateStoreAccess(store, userId, role);

        //기존 데이터와 다른 경우에만 검증 수행
        if (!Objects.equals(store.getCategoryId(), reqUpdateStoreDto.getCategoryId())) {
            categoryRepository.findById(reqUpdateStoreDto.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            store.changeCategory(reqUpdateStoreDto.getCategoryId());
        }

        if (!Objects.equals(store.getRegionId(), reqUpdateStoreDto.getRegionId())) {
            regionRepository.findById(reqUpdateStoreDto.getRegionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));
            store.changeRegion(reqUpdateStoreDto.getRegionId());
        }

        store.changeInfo(
                reqUpdateStoreDto.getName(),
                reqUpdateStoreDto.getAddress(),
                reqUpdateStoreDto.getPhone(),
                reqUpdateStoreDto.getOpenTime(),
                reqUpdateStoreDto.getCloseTime()
        );

        store.changeStatus(reqUpdateStoreDto.getStatus());

        String categoryName = resolveCategoryName(store.getCategoryId());
        String regionName = resolveRegionName(store.getRegionId());

        return ResGetStoreDto.from(store, categoryName, regionName);
    }

    //가게 삭제
    @Transactional
    public ResDeleteStoreDto deleteStore(UUID storeId, Long userId, Role role) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        validateStoreAccess(store, userId, role);

        if(store.isDeleted()){
            throw new BusinessException(ErrorCode.STORE_ALREADY_DELETED);
        }

        String storeName = store.getName();

        store.softDelete(userId);
        return ResDeleteStoreDto.from(store.getStoreId(), storeName);
    }

    //가게 검색
    @Transactional(readOnly = true)
    public Page<ResSearchStoreDto> searchStores(String keyword, UUID categoryId, StoreStatus status, Pageable pageable) {
        if (!StringUtils.hasText(keyword) && categoryId == null) {
            throw new BusinessException(ErrorCode.STORE_SEARCH_CONDITION_REQUIRED);
        }

        // categoryId로 검색하는 경우, 결과 매핑에 재사용할 수 있도록 카테고리명을 미리 조회해둔다
        String filteredCategoryName = null;
        if (categoryId != null) {
            filteredCategoryName = categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                    .map(Category::getName)
                    .orElse(null);
            if (filteredCategoryName == null) {
                return Page.empty(pageable);
            }
        }

        // categoryId가 이미 지정된 경우엔 "카테고리명이 keyword를 포함하는지"는 볼 필요가 없다.
        // (그 카테고리로 이미 필터링되므로, 남는 조건은 store.name만으로 좁혀야 함)
        List<UUID> keywordCategoryIds = (keyword == null || categoryId != null)
                ? List.of()
                : categoryRepository.findByNameContainingAndDeletedAtIsNull(keyword).stream()
                        .map(Category::getCategoryId)
                        .toList();

        String resolvedCategoryName = filteredCategoryName;
        Page<Store> storePage = storeRepository.searchStores(keyword, categoryId, keywordCategoryIds, status, pageable);

        // categoryId로 필터링된 경우 모든 결과가 같은 카테고리이므로 위에서 조회한 이름을 재사용하고,
        // keyword만으로 검색된 경우(가게마다 카테고리가 다를 수 있음)에만 한 번에 배치 조회해 N+1을 방지한다
        Map<UUID, String> categoryNames = resolvedCategoryName != null
                ? Map.of(categoryId, resolvedCategoryName)
                : resolveCategoryNames(storePage.getContent().stream().map(Store::getCategoryId).distinct().toList());
        Map<UUID, String> regionNames = resolveRegionNames(
                storePage.getContent().stream().map(Store::getRegionId).distinct().toList());

        return storePage.map(store -> ResSearchStoreDto.from(
                store,
                requireCategoryName(categoryNames, store.getCategoryId()),
                requireRegionName(regionNames, store.getRegionId())
        ));
    }

    private String resolveCategoryName(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private String resolveRegionName(UUID regionId) {
        return regionRepository.findById(regionId)
                .map(Region::getName)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));
    }

    private Map<UUID, String> resolveCategoryNames(List<UUID> categoryIds) {
        return categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));
    }

    private Map<UUID, String> resolveRegionNames(List<UUID> regionIds) {
        return regionRepository.findAllById(regionIds).stream()
                .collect(Collectors.toMap(Region::getRegionId, Region::getName));
    }

    private String requireCategoryName(Map<UUID, String> categoryNames, UUID categoryId) {
        String categoryName = categoryNames.get(categoryId);
        if (categoryName == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return categoryName;
    }

    private String requireRegionName(Map<UUID, String> regionNames, UUID regionId) {
        String regionName = regionNames.get(regionId);
        if (regionName == null) {
            throw new BusinessException(ErrorCode.REGION_NOT_FOUND);
        }
        return regionName;
    }

    // OWNER는 본인 소유 가게만, MANAGER·MASTER는 전체 접근 허용
    private void validateStoreAccess(Store store, Long userId, Role role) {
        switch (role) {
            case OWNER -> {
                if (!store.getUserId().equals(userId)) {
                    throw new BusinessException(ErrorCode.STORE_ACCESS_DENIED);
                }
            }
            case MANAGER, MASTER -> {
                // 전체 접근 허용
            }
            default -> throw new BusinessException(ErrorCode.STORE_ACCESS_DENIED);
        }
    }
}

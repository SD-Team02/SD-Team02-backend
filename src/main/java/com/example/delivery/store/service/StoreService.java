package com.example.delivery.store.service;

import com.example.delivery.category.repository.CategoryRepository;
import com.example.delivery.region.repository.RegionRepository;
import com.example.delivery.store.dto.request.ReqCreateStoreDto;
import com.example.delivery.store.dto.response.ResCreateStoreDto;
import com.example.delivery.store.dto.response.ResGetStoreDto;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.entity.StoreStatus;
import com.example.delivery.store.repository.StoreRepository;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        String categoryName = categoryRepository.findById(store.getCategoryId())
                .map(category -> category.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        String regionName = regionRepository.findById(store.getRegionId())
                .map(region -> region.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));

        return ResCreateStoreDto.from(store, categoryName, regionName);
    }

    //전체 가게 조회
    @Transactional(readOnly = true)
    public Page<ResGetStoreDto> getAllStores(StoreStatus status, Pageable pageable) {
        return storeRepository.findByStatusAndDeletedAtIsNull(status, pageable)
                .map(store -> {
                    String categoryName = categoryRepository.findById(store.getCategoryId())
                            .map(category -> category.getName())
                            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

                    String regionName = regionRepository.findById(store.getRegionId())
                            .map(region -> region.getName())
                            .orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND));

                    return ResGetStoreDto.from(store, categoryName, regionName);
                });
    }
}

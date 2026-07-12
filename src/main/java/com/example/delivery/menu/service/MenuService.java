package com.example.delivery.menu.service;

import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.image.entity.ImageFile;
import com.example.delivery.image.repository.ImageRepository;
import com.example.delivery.image.service.ImageService;
import com.example.delivery.menu.dto.request.MenuRequestDto;
import com.example.delivery.menu.dto.response.MenuResponseDto;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.AiHistoryRepository;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final AiHistoryRepository aiHistoryRepository;
    private final ImageService imageService;

    @Transactional
    public void createMenu(MenuRequestDto menuRequestDto, JpaAuditingConfig.CustomUserDetails userDetails) {

        // 일딴 확인 없이 create
        // store테이블 에서 sotorId 검색 없으면 에러출력
        // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
        //Store store = storeRepository.findById(menuRequestDto.getStoreId())
        //    .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        //validateMenuAccess(store, userDetails);

         Menu menu = new Menu(
            menuRequestDto.getStoreId(),        // UUID
            menuRequestDto.getMenuName(),       // String
            menuRequestDto.getPrice(),          // Integer
            menuRequestDto.getDescription(),    // String
            menuRequestDto.getAiGenerated()     // Boolean
        );


        try {
            menuRepository.save(menu);
            // 메뉴 등록시 refId 값 넣기
            //imageService.imageUpDate();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.MENU_SERVER_ERROR);
        }

    }

    @Transactional(readOnly = true)
    public List<MenuResponseDto> getMenuList(UUID storeId) {

        List<Menu> menus = menuRepository.findByStoreIdAndDeletedAtIsNull(storeId);

        return menus.stream()
                .map(MenuResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<MenuResponseDto> getAdminMenuList(MenuRequestDto menuRequestDto, JpaAuditingConfig.CustomUserDetails userDetails, Pageable pageable) {
        // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
        //if ("CUSTOMER".equals(userDetails.getRole()) || "OWNER".equals(userDetails.getRole())) {
        //    throw new BusinessException(ErrorCode.ACCESS_DENIED);
        //}

        Page<Menu> menus = menuRepository.searchAdminMenus(menuRequestDto, pageable);
        Page<MenuResponseDto> dtoPage = menus.map(MenuResponseDto::new);

        return PageResponse.from(dtoPage);
    }

    @Transactional
    public void updateMenu(MenuRequestDto menuRequestDto, JpaAuditingConfig.CustomUserDetails userDetails) {
        // 일딴 확인 없이 update
        // menu테이블 에서 menuId 검색 없으면 에러출력
        // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
        Menu menu = menuRepository.findById(menuRequestDto.getMenuId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        //Store store = storeRepository.findById(menu.getStoreId())
        //         .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        //validateMenuAccess(store, userDetails);

        // 엔티티가 가진 도메인 메서드로 필드만 변경
        // JPA가 자동으로 변경감지 Dirty Checking
        // 비교해서 변경된 경우만 update 쿼리 날림
        if (menuRequestDto.getPrice() != null) {
            menu.changePrice(menuRequestDto.getPrice());
        }

        if (menuRequestDto.getDescription() != null) {
            menu.changeDescription(menuRequestDto.getDescription(), Boolean.TRUE.equals(menuRequestDto.getAiGenerated()));
        }

        if(menuRequestDto.getMenuName() != null){
            menu.changeName(menuRequestDto.getMenuName());
        }

        if ("HIDDEN".equals(menuRequestDto.getMenuStatus())) {
            menu.hide();
        } else if ("NORMAL".equals(menuRequestDto.getMenuStatus())) {
            menu.show();
        }

    }

    @Transactional
    public void deleteMenu(UUID menuId, JpaAuditingConfig.CustomUserDetails userDetails) {
        // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        //Store store = storeRepository.findById(menu.getStoreId())
        //        .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        //validateMenuAccess(store, userDetails);

        menu.softDelete(userDetails.getUserId());
    }

    // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
    private void validateMenuAccess(Store store, JpaAuditingConfig.CustomUserDetails userDetails) {
        //boolean isAdmin = "MASTER".equals(userDetails.getRole()) || "MANAGER".equals(userDetails.getRole());
        //boolean isOwner = store.getUserId().equals(userDetails.getUserId());

        //if (!isAdmin && !isOwner) {
        //    throw new BusinessException(ErrorCode.ACCESS_DENIED);
        //}
    }

}






























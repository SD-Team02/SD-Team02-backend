package com.example.delivery.menu.service;

import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.image.service.ImageService;
import com.example.delivery.menu.dto.request.MenuRequestDto;
import com.example.delivery.menu.dto.response.MenuResponseDto;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.AiHistoryRepository;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.repository.StoreRepository;
import com.example.delivery.user.entity.Role;
import com.example.delivery.user.security.UserDetailsImpl;
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
    private final ImageService imageService;
    private final StoreRepository storeRepository;

    @Transactional
    public void createMenu(MenuRequestDto menuRequestDto, UserDetailsImpl userDetails) {

        // 일딴 확인 없이 create
        // store테이블 에서 sotorId 검색 없으면 에러출력
        // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
        // 2026-07-13
        // 코드리뷰 수정
        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(menuRequestDto.getStoreId())
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        validateMenuAccess(store, userDetails);

         Menu menu = new Menu(
            menuRequestDto.getStoreId(),        // UUID
            menuRequestDto.getMenuName(),       // String
            menuRequestDto.getPrice(),          // Integer
            menuRequestDto.getDescription(),    // String
            menuRequestDto.getAiGenerated()     // Boolean
        );


        try {
            Menu savedMenu = menuRepository.save(menu);
            // 메뉴 등록시 refId 값 넣기
            imageService.connectMenuImage(
                menuRequestDto.getImageId(),
                String.valueOf(savedMenu.getMenuId())
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.MENU_SERVER_ERROR);
        }

    }

    @Transactional(readOnly = true)
    public List<MenuResponseDto> getMenuList(UUID storeId, UUID menuId) {

        if(menuId != null) {
            Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
            return List.of(MenuResponseDto.from(menu));
        }

        if(storeId != null) {
            return menuRepository.findByStoreIdAndDeletedAtIsNull(storeId).stream()
                .map(MenuResponseDto::from)
                .toList();
        }

        throw new BusinessException(ErrorCode.INVALID_MENU_STATUS);
    }

    @Transactional(readOnly = true)
    public PageResponse<MenuResponseDto> getAdminMenuList(MenuRequestDto menuRequestDto, Pageable pageable) {

        Page<Menu> menus = menuRepository.searchAdminMenus(menuRequestDto, pageable);
        Page<MenuResponseDto> dtoPage = menus.map(MenuResponseDto::new);

        return PageResponse.from(dtoPage);
    }

    @Transactional
    public void updateMenu(MenuRequestDto menuRequestDto, UserDetailsImpl userDetails) {
        // 일딴 확인 없이 update
        // menu테이블 에서 menuId 검색 없으면 에러출력
        // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
        Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(menuRequestDto.getMenuId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(menu.getStoreId())
                 .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        validateMenuAccess(store, userDetails);

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

        // 2026-07-13
        // 코드리뷰 수정
        if ("HIDDEN".equals(menuRequestDto.getMenuStatus().toString())) {
            menu.hide();
        } else if ("NORMAL".equals(menuRequestDto.getMenuStatus().toString())) {
            menu.show();
        }

    }

    @Transactional
    public void deleteMenu(UUID menuId, UserDetailsImpl userDetails) {
        // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
        Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(menu.getStoreId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        validateMenuAccess(store, userDetails);

        // 2026-07-13
        // 코드리뷰 수정
        if(!menu.isDeleted()) {
            menu.softDelete(userDetails.getUser().getUserId());
        }else{
            throw new BusinessException(ErrorCode.MENU_IS_DELETE);
        }
    }

    // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
    private void validateMenuAccess(Store store, UserDetailsImpl userDetails) {
        boolean isOwner = store.getUserId().equals(userDetails.getUser().getUserId());

        if (!isOwner) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

}






























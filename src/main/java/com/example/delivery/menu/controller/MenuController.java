package com.example.delivery.menu.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.menu.dto.request.MenuRequestDto;
import com.example.delivery.menu.dto.response.MenuResponseDto;
import com.example.delivery.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/menus")
    public ResponseEntity<ApiResponse<Void>> createMenu(
            @RequestBody MenuRequestDto menuRequestDto,
            @AuthenticationPrincipal JpaAuditingConfig.CustomUserDetails userDetails){
        log.info("메뉴 등록 menuRequestDto : " + menuRequestDto);

        menuService.createMenu(menuRequestDto, userDetails);

        return ResponseEntity.ok(
            ApiResponse.successMessage("메뉴 등록이 완료되었습니다.")
        );
    }
    // 사용자 메뉴 검색
    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<MenuResponseDto>>> getMenuList(
           @RequestParam UUID storeId){

        log.info("사용자 메뉴 검색 storeId : " + storeId);
        List<MenuResponseDto> menuList = menuService.getMenuList(storeId);

        return ResponseEntity.ok(
                ApiResponse.success("메뉴 조회가 되었습니다", menuList)
        );
    }
    // 관리자 메뉴 검색
    @GetMapping("/menus/admin")
    public ResponseEntity<ApiResponse<PageResponse<MenuResponseDto>>> getAdminMenuList(
           MenuRequestDto menuRequestDto,
           @PageableDefault(size = 10) Pageable pageable,
           @AuthenticationPrincipal JpaAuditingConfig.CustomUserDetails userDetails){

        log.info("관리자 메뉴 검색 menuRequestDto : " + menuRequestDto + "\nuserDetails : userDetails" + "\npageable : " + pageable);
        PageResponse<MenuResponseDto> menuList = menuService.getAdminMenuList(menuRequestDto, userDetails, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("메뉴 조회가 되었습니다", menuList)
        );
    }
    // 메뉴 수정
    @PutMapping("/menus")
    public ResponseEntity<ApiResponse<Void>> updateMenu(
            @RequestBody MenuRequestDto menuRequestDto,
            @AuthenticationPrincipal JpaAuditingConfig.CustomUserDetails userDetails){
        log.info("메뉴 수정 menuRequestDto : " + menuRequestDto + "\n userDetails : " + userDetails);

        menuService.updateMenu(menuRequestDto, userDetails);

        return ResponseEntity.ok(
            ApiResponse.successMessage("메뉴 수정이 완료되었습니다.")
        );
    }
    //메뉴삭제
    @DeleteMapping("/menus")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @RequestParam UUID menuId,
            @AuthenticationPrincipal JpaAuditingConfig.CustomUserDetails userDetails
    ){
        log.info("메뉴 삭제 menuId : " + menuId + "\n userDetails : " + userDetails);

        menuService.deleteMenu(menuId, userDetails);

        return ResponseEntity.ok(
            ApiResponse.successMessage("메뉴가 삭제되었습니다.")
        );
    }
}

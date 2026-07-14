package com.example.delivery.menu.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.menu.dto.request.MenuRequestDto;
import com.example.delivery.menu.dto.response.MenuResponseDto;
import com.example.delivery.menu.service.MenuService;
import com.example.delivery.user.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 메뉴등록
    // 2026-07-13
    // 코드리뷰 수정
    @PostMapping("/menus")
    @PreAuthorize("hasAnyAuthority('OWNER','MANAGER','MASTER')")
    @Tag(name = "메뉴", description = "메뉴 등록 API")
    public ResponseEntity<ApiResponse<Void>> createMenu(
            @RequestBody MenuRequestDto menuRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        log.info("메뉴 등록 menuRequestDto : " + menuRequestDto);

        menuService.createMenu(menuRequestDto, userDetails);

        return ResponseEntity.ok(
            ApiResponse.successMessage("메뉴 등록이 완료되었습니다.")
        );
    }
    // 사용자 가게 메뉴 검색, 단건 검색
    @GetMapping("/menus")
    @Tag(name = "메뉴", description = "가게 메뉴 조회 API")
    public ResponseEntity<ApiResponse<List<MenuResponseDto>>> getMenuList(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID menuId){

        log.info("사용자 메뉴 검색 storeId : " + storeId);
        log.info("사용자 메뉴 검색 menuId : " + menuId);

        List<MenuResponseDto> menuList = menuService.getMenuList(storeId, menuId);

        return ResponseEntity.ok(
                ApiResponse.success("메뉴 조회가 되었습니다", menuList)
        );
    }
    // 관리자 메뉴 검색
    // 2026-07-13
    // 코드리뷰 수정
    @GetMapping("/menus/admin")
    @PreAuthorize("hasAnyAuthority('MANAGER','MASTER')")
    @Tag(name = "메뉴", description = "관리자용 메뉴 조회 API")
    public ResponseEntity<ApiResponse<PageResponse<MenuResponseDto>>> getAdminMenuList(
           MenuRequestDto menuRequestDto,
           @PageableDefault(size = 10) Pageable pageable,
           @AuthenticationPrincipal UserDetailsImpl userDetails){

        log.info("관리자 메뉴 검색 menuRequestDto : " + menuRequestDto + "\nuserDetails : userDetails" + "\npageable : " + pageable);
        PageResponse<MenuResponseDto> menuList = menuService.getAdminMenuList(menuRequestDto, userDetails, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("메뉴 조회가 되었습니다", menuList)
        );
    }
    // 메뉴 수정
    // 2026-07-13
    // 코드리뷰 수정
    @PutMapping("/menus")
    @PreAuthorize("hasAnyAuthority('OWNER','MANAGER','MASTER')")
    @Tag(name = "메뉴", description = "메뉴 수정 API")
    public ResponseEntity<ApiResponse<Void>> updateMenu(
            @RequestBody MenuRequestDto menuRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        log.info("메뉴 수정 menuRequestDto : " + menuRequestDto + "\n userDetails : " + userDetails);

        menuService.updateMenu(menuRequestDto, userDetails);

        return ResponseEntity.ok(
            ApiResponse.successMessage("메뉴 수정이 완료되었습니다.")
        );
    }
    // 메뉴삭제
    // 2026-07-13
    // 코드리뷰 수정
    @DeleteMapping("/menus")
    @PreAuthorize("hasAnyAuthority('OWNER','MANAGER','MASTER')")
    @Tag(name = "메뉴", description = "메뉴 삭제 API")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @RequestParam UUID menuId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        log.info("메뉴 삭제 menuId : " + menuId + "\n userDetails : " + userDetails);

        menuService.deleteMenu(menuId, userDetails);

        return ResponseEntity.ok(
            ApiResponse.successMessage("메뉴가 삭제되었습니다.")
        );
    }
}

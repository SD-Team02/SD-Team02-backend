package com.example.delivery.menu.repository;

import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.menu.dto.request.MenuRequestDto;
import com.example.delivery.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MenuRepositoryCustom {

    // 관리자에서 메뉴 검색
    Page<Menu> searchAdminMenus(MenuRequestDto menuRequestDto, Pageable pageable);
}

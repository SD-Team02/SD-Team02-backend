package com.example.delivery.category.service;

import com.example.delivery.category.dto.ReqCreateCategoryDto;
import com.example.delivery.category.dto.ReqUpdateCategoryDto;
import com.example.delivery.category.dto.ResGetCategoryDto;
import com.example.delivery.category.dto.ResCreateCategoryDto;
import com.example.delivery.category.dto.ResUpdateCategoryDto;
import com.example.delivery.category.entity.Category;
import com.example.delivery.category.entity.CategoryStatus;
import com.example.delivery.category.repository.CategoryRepository;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 생성 - 성공")
    void createCategory_success() {
        // given
        ReqCreateCategoryDto dto = new ReqCreateCategoryDto();
        ReflectionTestUtils.setField(dto, "name", "한식");
        
        Category category = new Category("한식");
        
        when(categoryRepository.existsByName(any())).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(category);

        // when
        ResCreateCategoryDto result = categoryService.createCategory(dto);

        // then
        assertThat(result.getName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("카테고리 생성 - 중복 시 예외 발생")
    void createCategory_duplicate_throwsException() {
        // given
        ReqCreateCategoryDto dto = new ReqCreateCategoryDto();
        ReflectionTestUtils.setField(dto, "name", "한식");
        
        when(categoryRepository.existsByName(any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("카테고리 생성 - DB 중복 시 예외 발생")
    void createCategory_dbDuplicate_throwsException() {
        // given
        ReqCreateCategoryDto dto = new ReqCreateCategoryDto();
        ReflectionTestUtils.setField(dto, "name", "한식");

        when(categoryRepository.existsByName(any())).thenReturn(false);
        when(categoryRepository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate"));

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("전체 카테고리 조회 - 성공")
    void getCategories_success() {
        // given
        Category category = new Category("한식");
        ReflectionTestUtils.setField(category, "categoryId", UUID.randomUUID());
        
        Page<Category> categoryPage = new PageImpl<>(List.of(category), PageRequest.of(0, 10), 1);
        
        when(categoryRepository.findAllByStatus(any(), any())).thenReturn(categoryPage);

        // when
        Page<ResGetCategoryDto> result = categoryService.getAllCategories(CategoryStatus.ACTIVE, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("카테고리 상세 조회 - 성공")
    void getCategory_success() {
        // given
        Category category = new Category("한식");
        UUID categoryId = UUID.randomUUID();
        ReflectionTestUtils.setField(category, "categoryId", categoryId);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        ResGetCategoryDto result = categoryService.getCategory(categoryId);

        // then
        assertThat(result.getCategoryId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("카테고리 상세 조회 - 존재하지 않을 때 예외 발생")
    void getCategory_notFound_throwsException() {
        // given
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("카테고리 수정 - 성공")
    void updateCategory_success() {
        // given
        UUID categoryId = UUID.randomUUID();
        Category category = new Category("한식");
        ReflectionTestUtils.setField(category, "categoryId", categoryId);
        
        ReqUpdateCategoryDto dto = new ReqUpdateCategoryDto();
        ReflectionTestUtils.setField(dto, "name", "일식");
        ReflectionTestUtils.setField(dto, "status", CategoryStatus.INACTIVE);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        ResUpdateCategoryDto result = categoryService.updateCategory(categoryId, dto);

        // then
        assertThat(result.getName()).isEqualTo("일식");
        assertThat(result.getStatus()).isEqualTo(CategoryStatus.INACTIVE);
    }

    @Test
    @DisplayName("카테고리 수정 - 존재하지 않을 때 예외 발생")
    void updateCategory_notFound_throwsException() {
        // given
        UUID categoryId = UUID.randomUUID();
        ReqUpdateCategoryDto dto = new ReqUpdateCategoryDto();
        ReflectionTestUtils.setField(dto, "name", "일식");
        ReflectionTestUtils.setField(dto, "status", CategoryStatus.ACTIVE);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }
}

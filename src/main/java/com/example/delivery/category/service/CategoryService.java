package com.example.delivery.category.service;

import com.example.delivery.category.dto.request.ReqCreateCategoryDto;
import com.example.delivery.category.dto.request.ReqUpdateCategoryDto;
import com.example.delivery.category.dto.response.ResCreateCategoryDto;
import com.example.delivery.category.dto.response.ResDeleteCategoryDto;
import com.example.delivery.category.dto.response.ResGetCategoryDto;
import com.example.delivery.category.dto.response.ResUpdateCategoryDto;
import com.example.delivery.category.entity.Category;
import com.example.delivery.category.entity.CategoryStatus;
import com.example.delivery.category.repository.CategoryRepository;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    //카테고리 생성
    @Transactional
    public ResCreateCategoryDto createCategory(ReqCreateCategoryDto reqCreateCategoryDto){
        checkDuplicate(reqCreateCategoryDto.getName());

        try {
            Category category = new Category(reqCreateCategoryDto.getName());

            return ResCreateCategoryDto.from(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    //전체 카테고리 조회
    @Transactional(readOnly = true)
    public Page<ResGetCategoryDto> getAllCategories(CategoryStatus status, Pageable pageable) {
        return categoryRepository.findAllByStatusAndDeletedAtIsNull(status, pageable)
                .map(ResGetCategoryDto::from);
    }

    //카테고리 상세 조회
    @Transactional(readOnly = true)
    public ResGetCategoryDto getCategory(UUID categoryId) {
        return categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                .map(ResGetCategoryDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    //카테고리 수정
    @Transactional
    public ResUpdateCategoryDto updateCategory(UUID categoryId, ReqUpdateCategoryDto reqUpdateCategoryDto) {
        // 삭제된 카테고리는 수정 대상에서 제외 (soft delete 정합성)
        Category category = categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getName().equals(reqUpdateCategoryDto.getName())) {
            if (categoryRepository.existsByNameAndCategoryIdNot(reqUpdateCategoryDto.getName(), categoryId)) {
                throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS);
            }
        }

        category.update(reqUpdateCategoryDto.getName(), reqUpdateCategoryDto.getStatus());
        categoryRepository.saveAndFlush(category);

        return ResUpdateCategoryDto.from(category);
    }

    //카테고리 삭제
    @Transactional
    public ResDeleteCategoryDto deleteCategory(UUID categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.isDeleted()) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_DELETED);
        }

        category.softDelete(userId);
        return ResDeleteCategoryDto.from(category);
    }


    /*
    [공통 메서드]
     */
    //카테고리명 중복 여부 확인 method
    private void checkDuplicate(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

}

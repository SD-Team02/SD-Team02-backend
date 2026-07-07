package com.example.delivery.category.service;

import com.example.delivery.category.dto.ReqCreateCategoryDto;
import com.example.delivery.category.dto.ResGetCategoryDto;
import com.example.delivery.category.dto.ResCreateCategoryDto;
import com.example.delivery.category.entity.Category;
import com.example.delivery.category.entity.CategoryStatus;
import com.example.delivery.category.repository.CategoryRepository;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    //카테고리 생성
    @Transactional
    public ResCreateCategoryDto createCategory(ReqCreateCategoryDto reqCreateCategoryDto){
        //중복 체크
        if (categoryRepository.existsByName(reqCreateCategoryDto.getName())) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        //entity 생성
        Category category = new Category(reqCreateCategoryDto.getName());

        //저장
        Category savedCategory = categoryRepository.save(category);

        //response DTO 반환
        return ResCreateCategoryDto.builder()
                .categoryId(savedCategory.getCategoryId())
                .name(savedCategory.getName())
                .status(savedCategory.getStatus())
                .createdAt(savedCategory.getCreatedAt())
                .build();
    }

    //전체 카테고리 조회
    @Transactional(readOnly = true)
    public Page<ResGetCategoryDto> getAllCategories(CategoryStatus status, Pageable pageable) {
        return categoryRepository.findAllByStatus(status, pageable)
                .map(ResGetCategoryDto::from);
    }
}

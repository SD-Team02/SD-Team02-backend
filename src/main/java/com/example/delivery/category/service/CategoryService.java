package com.example.delivery.category.service;

import com.example.delivery.category.dto.ReqCreateCategoryDto;
import com.example.delivery.category.dto.ResCreateCategoryDto;
import com.example.delivery.category.entity.Category;
import com.example.delivery.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    //카테고리 생성
    public ResCreateCategoryDto createCategory(ReqCreateCategoryDto reqCreateCategoryDto){
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
}

package com.example.delivery.image.repository;

import com.example.delivery.image.dto.request.ImageRequestDto;
import com.example.delivery.image.entity.ImageFile;

import java.util.List;

public interface ImageRepositoryCustom {
    // 조건에 맞는 이미지 목록 조회
    List<ImageFile> findImages(ImageRequestDto imageRequestDto);
}

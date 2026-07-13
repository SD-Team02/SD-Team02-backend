package com.example.delivery.image.service;

import com.example.delivery.image.dto.S3UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3StorageService {

    // 다건 이미지 업로드
    List<S3UploadResult> upload(List<MultipartFile> files);

    // 이미지 삭제
    void delete(String imageKey);

    // Presigned URL 생성
    String generatePresignedUrl(String imageKey);
}
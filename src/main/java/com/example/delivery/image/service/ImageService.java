package com.example.delivery.image.service;

import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.image.dto.S3UploadResult;
import com.example.delivery.image.dto.request.ImageRequestDto;
import com.example.delivery.image.dto.response.ImageResponseDto;
import com.example.delivery.image.entity.ImageDisplayStatus;
import com.example.delivery.image.entity.ImageFile;
import com.example.delivery.image.repository.ImageRepository;
import com.example.delivery.user.security.UserDetailsImpl;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3StorageService s3StorageService;

    // 2026-07-13
    // 코드리뷰 수정
    @Transactional
    public List<ImageResponseDto> imageUpLoad (ImageRequestDto request, List<MultipartFile> files) {

        List<S3UploadResult> uploadResults = new ArrayList<>();

        try {
            // 이미지를 S3에 업로드
            uploadResults = s3StorageService.upload(files);

            List<ImageResponseDto> responses = new ArrayList<>();

            // 이미지 엔티티 생성
           for (int i = 0; i < uploadResults.size(); i++) {
               S3UploadResult result = uploadResults.get(i);

               // 이미지 엔티티 생성
               ImageFile imageFile = new ImageFile(
                       request.getRefType(),
                       request.getRefId(),
                       result.imageKey(),
                       result.originalName(),
                       result.savedName(),
                       result.contentType(),
                       result.fileSize(),
                       i+1
               );
               // 업로드가 먼저 되었을때 숨김 처리
               if(StringUtils.hasText(imageFile.getRefId())) imageFile.hide();

               // 이미지 정보를 DB에 저장
               ImageFile savedImage = imageRepository.save(imageFile);

               // 이미지 접근 URL 생성
               String imageUrl = s3StorageService.generatePresignedUrl(savedImage.getImageKey());

               // 응답 목록 추가
               responses.add(
                       new ImageResponseDto(savedImage, imageUrl)
               );
           }

           return responses;

        } catch (Exception e) {
            // DB 저장 실패 시 S3 이미지 삭제
            uploadResults.forEach(result ->
                   s3StorageService.delete(result.imageKey())
            );

            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    // 이미지 가져오기
    @Transactional(readOnly = true)
    public List<ImageResponseDto> getImages(ImageRequestDto imageRequestDto) {

        // 조회 조건 검증
        if (imageRequestDto.getImageId() == null
                && imageRequestDto.getRefType() == null
                && !StringUtils.hasText(imageRequestDto.getRefId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 조건에 맞는 이미지 조회
        List<ImageFile> images = imageRepository.findImages(imageRequestDto);

        return images.stream()
            .map(image -> {
                // S3 이미지에 접근할 수 있는 임시 URL 생성
                String imageUrl = s3StorageService.generatePresignedUrl(image.getImageKey());
                return new ImageResponseDto(image, imageUrl);
            })
            .toList();
    }
    // 이미지 삭제
    @Transactional
    public void deleteImage(UUID imageId, UserDetailsImpl userDetails) {

        ImageFile imageFile = imageRepository.findByImageIdAndDeletedAtIsNull(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_IMAGE_DELETE_FILE));

        //권한 확인
        if(!(userDetails.getUser().getUserId()).equals(imageFile.getCreatedBy())){
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        imageFile.softDelete(userDetails.getUser().getUserId());

    }
    // 업로드된 이미지에 refId 값넣기
    @Transactional
    public void connectMenuImage(UUID imageId, String refId) {

        // 이미지가 없으면 연결하지 않음
        if (imageId == null) {
            return;
        }

        // 이미지 조회
        ImageFile imageFile = imageRepository.findByImageIdAndDeletedAtIsNull(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 메뉴 이미지로 연결
        imageFile.changeRefId(
                refId
        );

        // refId 들어왔을때 이미지 보이게 변경
        if(!StringUtils.hasText(imageFile.getRefId())) imageFile.show();

    }
    // LLM이 이미지 받을수 있도록 서비스 생성
    public Resource getImageResource(@NotNull(message = "메뉴 이미지 ID는 필수입니다.") UUID imageId) {
         // 이미지 조회
        ImageFile image = imageRepository.findByImageIdAndDeletedAtIsNull(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 삭제된 이미지 확인
        if (image.isDeleted() || image.getDisplayStatus() == ImageDisplayStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        // 이미지 URL 생성
        String imageUrl = s3StorageService.generatePresignedUrl(image.getImageKey());

        try {
            // 이미지 Resource 생성
            return new UrlResource(imageUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.IMAGE_URL_GENERATION_FAILED);
        }
    }
}

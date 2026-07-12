package com.example.delivery.image.dto.response;

import com.example.delivery.image.entity.ImageDisplayStatus;
import com.example.delivery.image.entity.ImageFile;
import com.example.delivery.menu.dto.response.AiHistoryResponseDto;
import com.example.delivery.menu.entity.AiHistory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ImageResponseDto {
    private UUID imageId;                       // 이미지 Id
    private ImageFile.RefType refType;          // 참조 도메인 (MENU, STORE, USER)
    private String refId;                       // 참조 대상 ID (MENU_ID, STORE_ID, USER_ID)
    private String imageUrl;                    // 이미지 접근 url
    private String originalName;                // 업로드 원본 파일명
    private String savedName;                   // 서버에 저장된 파일명
    private String contentType;                 // MIME 타입(image/jpeg, image/png, image/webp)
    private Long fileSize;                      // 파일 크기(byte)
    private Integer displayOrder;               // 여러 장일 때 노출 순서 (1번째가 대표이미지)
    private ImageDisplayStatus displayStatus;   // 이미지 상태값 (정상: NORMAL, 숨김: HIDDEN)
    private LocalDateTime createdAt;            // 생성일시
    private Long createdBy;                     // 생성자
    private LocalDateTime deletedAt;            // 삭제일시
    private Long deletedBy;                     // 삭제자

    public ImageResponseDto(ImageFile imageFile, String imageUrl) {
        this.imageId = imageFile.getImageId();
        this.refType = imageFile.getRefType();
        this.refId = imageFile.getRefId();
        this.imageUrl = imageUrl;
        this.originalName = imageFile.getOriginalName();
        this.savedName = imageFile.getSavedName();
        this.contentType = imageFile.getContentType();
        this.fileSize = imageFile.getFileSize();
        this.displayOrder = imageFile.getDisplayOrder();
        this.displayStatus = imageFile.getDisplayStatus();
        this.createdAt = imageFile.getCreatedAt();
        this.createdBy = imageFile.getCreatedBy();
        this.deletedAt = imageFile.getDeletedAt();
        this.deletedBy = imageFile.getDeletedBy();
    }
}


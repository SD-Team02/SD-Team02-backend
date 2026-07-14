package com.example.delivery.image.dto.request;

import com.example.delivery.image.entity.ImageDisplayStatus;
import com.example.delivery.image.entity.ImageFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class ImageRequestDto {

    private UUID imageId;                       // 이미지 Id
    private ImageFile.RefType refType;          // 참조 도메인 (MENU, STORE, USER)
    private String refId;                       // 참조 대상 ID (MENU_ID, STORE_ID, USER_ID)
    //private Integer displayOrder;               // 여러 장일 때 노출 순서 (1번째가 대표이미지)

}

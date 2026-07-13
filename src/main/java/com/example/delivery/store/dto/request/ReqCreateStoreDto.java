package com.example.delivery.store.dto.request;

import com.example.delivery.store.entity.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "가게 등록 요청 DTO")
public class ReqCreateStoreDto {
    @NotNull(message = "카테고리 선택은 필수입니다.")
    @Schema(description = "카테고리 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID categoryId;

    @NotNull(message = "지역 선택은 필수입니다.")
    @Schema(description = "지역 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID regionId;

    @NotBlank(message = "가게명은 필수입니다.")
    @Size(max = 100, message = "가게명은 100자 이하로 입력해주세요.")
    @Schema(description = "가게명", example = "엽기떡볶이 문정점")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 255, message = "주소는 255자 이하로 입력해주세요.")
    @Schema(description = "주소", example = "서울 송파구 문정동 620")
    private String address;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Size(max = 30, message = "전화번호는 30자 이하로 입력해주세요.")
    @Schema(description = "전화번호", example = "01011112222")
    private String phone;

    @NotNull(message = "오픈시간은 필수입니다.")
    @Schema(description = "오픈시간", example = "09:00")
    private LocalTime openTime;

    @NotNull(message = "마감시간은 필수입니다.")
    @Schema(description = "마감시간", example = "09:00")
    private LocalTime closeTime;

    @Schema(description = "영업 상태", example = "OPEN")
    private StoreStatus status;
}

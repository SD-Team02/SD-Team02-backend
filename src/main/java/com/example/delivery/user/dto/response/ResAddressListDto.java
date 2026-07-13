package com.example.delivery.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Schema(description = "주소 목록 응답")
public class ResAddressListDto {

    @NotNull
    @Schema(description = "유저 고유 Id", example = "5")
    private Long userId;

    @NotNull
    @Schema(description = "주소 고유 Id", example = "aaaa-bb11-ccc")
    private UUID addressId;

    @Schema(description = "주소", example = "서울시")
    private String address;

    @Schema(description = "상세 주소", example = "종로구 사직로 161 ")
    private String detailAddress;
}

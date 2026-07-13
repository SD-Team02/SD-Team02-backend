package com.example.delivery.user.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "주소 등록 요청")
public class ReqCreateAddressDto {

    @NotBlank(message = "주소를 입력해 주세요.")
    @Size(max = 150, message = "주소는 150자 이하로 입력해주세요.")
    @Schema(description = "주소", example = "서울시")
    private String address;

    @NotBlank(message = "상세 주소를 입력해 주세요.")
    @Size(max = 150, message = "상세 주소는 150자 이하로 입력해주세요.")
    @Schema(description = "상세 주소", example = "종로구 사직로 161 ")
    private String detailAddress;


}

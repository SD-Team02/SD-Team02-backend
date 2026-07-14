package com.example.delivery.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Schema(description = "주소 수정 응답")
public class ResUpdateAddressDto {

    private UUID addressId;

    private String Address;

    private String detailAddress;
}

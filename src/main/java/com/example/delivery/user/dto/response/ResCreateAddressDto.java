package com.example.delivery.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Schema(description = "주소 생성 응답")
public class ResCreateAddressDto {

    private UUID addressId;
}

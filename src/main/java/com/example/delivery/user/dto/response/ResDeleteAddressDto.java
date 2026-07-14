package com.example.delivery.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "주소 삭제 응답")
public class ResDeleteAddressDto {

    private UUID addressId;

}

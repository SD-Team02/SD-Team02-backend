package com.example.delivery.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ResAddressListDto {

    private Long userId;

    private UUID addressId;

    private String address;

    private String detailAddress;
}

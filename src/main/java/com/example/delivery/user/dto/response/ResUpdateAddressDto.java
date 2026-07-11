package com.example.delivery.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ResUpdateAddressDto {

    private UUID addressId;

    private String Address;

    private String detailAddress;
}

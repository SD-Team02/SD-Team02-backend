package com.example.delivery.user.dto.request;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ReqUpdateAddressDto {

    private UUID addressId;

    private String address;

    private String detailAddress;

}

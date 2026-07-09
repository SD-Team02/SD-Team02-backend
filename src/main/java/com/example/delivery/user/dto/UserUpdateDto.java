package com.example.delivery.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateDto {
    private String nickname;
    private String email;
    private String phone;
}

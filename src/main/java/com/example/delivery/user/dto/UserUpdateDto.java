package com.example.delivery.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateDto {
    @NotBlank
    private String nickname;
    @NotBlank
    private String email;
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
}

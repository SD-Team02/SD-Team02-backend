package com.example.delivery.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateDto {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = 20, message = "닉네임 20자 이하로 입력해주세요.")
    @Schema(description = "닉네임", example = "SPCX300gogo")
    private String nickname;

    @NotBlank(message = "이메일 입력해 주세요.")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
    @Schema(description = "이메일", example = "test@test.com")
    private String email;

    @NotBlank(message = "전화번호를 입력해 주세요.")
    @Size(max = 11, message = "번호는 11자 이하로 입력해주세요.")
    @Schema(description = "전화번호", example = "01011111111")
    private String phone;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하로 입력해주세요.")
    @Schema(description = "비밀번호", example = "11111111!")
    private String password;
}

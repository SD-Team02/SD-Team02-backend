package com.example.delivery.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
public class SignupRequestDto {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String nickname;

    @NotBlank
    private String phone;

    private String role;

    private boolean owner = false;
    private boolean manager = false;
    private boolean master = false;

    private String ownerToken = "";
    private String managerToken = "";
    private String masterToken = "";
}


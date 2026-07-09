package com.example.delivery.user.dto;

import com.example.delivery.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfoDto {
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private Boolean deleted;

    public UserInfoDto(String username, String nickname, String email, String phone, String role, Boolean deleted) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.deleted = deleted;
    }

    public UserInfoDto(User user) {
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole().name();
        this.deleted = user.getDeleted();
    }

}
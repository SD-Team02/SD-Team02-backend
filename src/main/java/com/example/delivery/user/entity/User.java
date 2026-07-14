package com.example.delivery.user.entity;

import com.example.delivery.global.common.entity.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PK는 팀 컨벤션(UUID)의 예외로 auto-increment Long을 사용한다 (ERD 확정 사항).
 * BaseEntity의 deletedAt/deletedBy는 감사 이력용으로 그대로 상속받되,
 * 실제 삭제(회원 탈퇴) 여부 판단 기준은 별도 컬럼인 deleted(is_deleted)로 한다 (ERD 확정 사항).
 *
 * email/nickname은 단독 유니크가 아니라 각각 (컬럼, is_deleted) 복합 유니크로 건다.
 * 탈퇴한 계정의 이메일/닉네임이 영구히 "사용중"으로 잠기는 것을 막고, 탈퇴 후 재사용할 수 있게 하기 위함이다.
 */
@Getter
@Entity
@Table(name = "p_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "is_deleted"}),
        @UniqueConstraint(columnNames = {"nickname", "is_deleted"})
})
@AttributeOverride(name = "createdBy", column = @Column(name = "created_by", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100, unique = true, nullable = false)
    private String username;

    @Column(name = "nickname", length = 100, nullable = false)
    private String nickname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "phone", length = 11, nullable = false)
    private String phone;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_deleted")
    private Boolean deleted = false;

    public User(String username, String nickname, String email, String password, Role role, String phone) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.deleted = false;
    }

    public void changeProfile(String nickname, String phone) {
        this.nickname = nickname;
        this.phone = phone;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    /** 회원 탈퇴. is_deleted를 삭제 판단 기준으로 삼고, BaseEntity의 deletedAt/deletedBy도 감사 이력으로 함께 남긴다. */
    public void withdraw(Long deletedByUserId) {
        this.deleted = true;
        softDelete(deletedByUserId);
    }

    @Override
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    public void update(String nickname, String email, String phone,String password ,Long userId) {
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.updatedBy = userId;
    }

}

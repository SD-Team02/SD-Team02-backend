package com.example.delivery.user.repository;

import com.example.delivery.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByNicknameAndDeletedFalse(String nickname);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByPhoneAndDeletedFalse(String phone);

    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(Long userId);
}

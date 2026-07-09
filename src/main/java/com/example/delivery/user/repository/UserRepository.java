package com.example.delivery.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}

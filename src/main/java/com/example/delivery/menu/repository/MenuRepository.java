package com.example.delivery.menu.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.menu.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
}

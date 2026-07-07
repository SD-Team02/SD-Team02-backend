package com.example.delivery.category.repository;

import com.example.delivery.category.entity.Category;
import com.example.delivery.category.entity.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(String name);
    Page<Category> findAllByStatus(CategoryStatus status, Pageable pageable);
}

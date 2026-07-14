package com.example.delivery.category.repository;

import com.example.delivery.category.entity.Category;
import com.example.delivery.category.entity.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(String name);
    boolean existsByNameAndCategoryIdNot(String name, UUID categoryId);
    Page<Category> findAllByStatusAndDeletedAtIsNull(CategoryStatus status, Pageable pageable);
    Optional<Category> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);
    List<Category> findByNameContainingAndDeletedAtIsNull(String name);
    List<Category> findByCategoryIdInAndDeletedAtIsNull(List<UUID> categoryIds);
}

package com.example.delivery.image.repository;


import com.example.delivery.image.entity.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageFile, UUID>, ImageRepositoryCustom {

    Optional<ImageFile> findByImageIdAndDeletedAtIsNull(UUID imageId);
}
